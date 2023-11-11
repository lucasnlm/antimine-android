#include <jni.h>

#include <algorithm>
#include <functional>

#include <cassert>
#include <string>
#include <cstdint>
#include <iostream>
#include <list>

#include "common.h"
#include "tree234.h"
#include "random_generator.h"
#include "minefield_creator.h"

#define sresize(array, number, type) \
    ( (type *) srealloc ((array), (number) * sizeof (type)) )

#define lenof(array) ( sizeof(array) / sizeof(*(array)) )

struct set {
    std::size_t x;
    std::size_t y;
    std::size_t mask;
    std::size_t mines;
    bool todo;
    struct set *prev, *next;
};

struct set_store {
    tree234 *sets;
    struct set *todo_head, *todo_tail;
};

typedef std::function<int(mine_context &, std::size_t, std::size_t)> open_function;

typedef std::function<void(mine_context &, std::basic_string<std::int8_t> &,
                           int, int, int)> perturbation_function;

static int set_comparator(void *av, void *bv) {
    auto *a = (struct set *) av;
    auto *b = (struct set *) bv;

    if (a->y < b->y) {
        return -1;
    } else if (a->y > b->y) {
        return +1;
    } else if (a->x < b->x) {
        return -1;
    } else if (a->x > b->x) {
        return +1;
    } else if (a->mask < b->mask) {
        return -1;
    } else if (a->mask > b->mask) {
        return +1;
    } else {
        return 0;
    }
}

int compare_square(const square &a, const square &b) {
    if (a.type < b.type || a.random < b.random || a.y < b.y || a.x < b.x) {
        return -1;
    } else if (a.type > b.type || a.random > b.random || a.y > b.y || a.x > b.x) {
        return +1;
    } else {
        return 0;
    }
}

int mine_open(mine_context &ctx, std::size_t x, std::size_t y) {
    int result = 0;

    assert(x >= 0 && x < ctx.width && y >= 0 && y < ctx.height);
    if (ctx.grid[y * ctx.width + x]) {
        return -1; // bang!
    }

    for (int i = -1; i <= +1; i++) {
        if (x + i < 0 || x + i >= ctx.width) {
            continue;
        }
        for (int j = -1; j <= +1; j++) {
            if (y + j < 0 || y + j >= ctx.height) {
                continue;
            }
            if (i == 0 && j == 0) {
                continue;
            }
            if (ctx.grid[(y + j) * ctx.width + (x + i)]) {
                result++;
            }
        }
    }

    return result;
}

void mine_perturbation(
        mine_context &ctx,
        std::basic_string<std::int8_t> &grid,
        int setx,
        int sety,
        int mask
) {
    std::size_t number_of_full = 0;
    std::size_t number_of_empty = 0;

    std::vector<square>& square_list = ctx.square_list;
    square_list.reserve(ctx.size);
    square_list.clear();

    std::vector<perturbation>& changes = ctx.changes;
    changes.clear();

    std::vector<std::size_t>& to_fill = ctx.to_fill;
    to_fill.clear();

    std::vector<std::size_t>& to_empty = ctx.to_empty;
    to_fill.clear();

    std::vector<std::size_t>& set_list = ctx.set_list;
    set_list.clear();

    if (!mask && !ctx.allow_big_perturbs) {
        return;
    }

    /*
     * Make a list of all the squares in the grid which we can
     * possibly use. This list should be in preference order, which
     * means
     *
     *  - first, unknown squares on the boundary of known space
     *  - next, unknown squares beyond that boundary
     * 	- as a very last resort, known squares, but not within one
     * 	  square of the starting position.
     *
     * Each of these sections needs to be shuffled independently.
     * We do this by preparing list of all squares and then sorting
     * it with a random secondary key.
     */
    for (std::size_t y = 0; y < ctx.height; y++) {
        for (std::size_t x = 0; x < ctx.width; x++) {
            square current{
                    .id = square_list.size(),
                    .x = x,
                    .y = y
            };

            /*
             * If this square is too near the starting position,
             * don't put it on the list at all.
             */
            int dy = static_cast<int>(y) - static_cast<int>(ctx.start_y);
            int dx = static_cast<int>(x) - static_cast<int>(ctx.start_x);

            if (abs(dy) <= 1 && abs(dx) <= 1) {
                continue;
            }

            /*
             * If this square is in the input set, also don't put
             * it on the list!
             */
            if ((mask == 0 && grid[y * ctx.width + x] == -2) ||
                (x >= setx && x < setx + 3 &&
                 y >= sety && y < sety + 3 &&
                 mask & (1 << ((y - sety) * 3 + (x - setx)))))
                continue;

            if (grid[y * ctx.width + x] != -2) {
                current.type = 3;    /* known square */
            } else {
                /*
                 * Unknown square. Examine everything around it and
                 * see if it borders on any known squares. If it
                 * does, it's class 1, otherwise it's 2.
                 */

                current.type = 2;

                for (dy = -1; dy <= +1; dy++) {
                    for (dx = -1; dx <= +1; dx++) {
                        if (x + dx >= 0 && x + dx < ctx.width &&
                            y + dy >= 0 && y + dy < ctx.height &&
                            grid[(y + dy) * ctx.width + (x + dx)] != -2) {
                            current.type = 1;
                            break;
                        }
                    }
                }
            }

            /*
             * Finally, a random number to cause qsort to
             * shuffle within each group.
             */
            current.random = random_bits(ctx.random);
            square_list.push_back(current);
        }
    }

    std::sort(square_list.begin(), square_list.end(), compare_square);

    // Now count up the number of full and empty squares in the set we've been provided.
    if (mask) {
        for (int dy = 0; dy < 3; dy++) {
            for (int dx = 0; dx < 3; dx++) {
                if (mask & (1 << (dy * 3 + dx))) {
                    assert(setx + dx <= ctx.width);
                    assert(sety + dy <= ctx.height);
                    if (ctx.grid[(sety + dy) * ctx.width + (setx + dx)]) {
                        number_of_full++;
                    } else {
                        number_of_empty++;
                    }
                }
            }
        }
    } else {
        for (int y = 0; y < ctx.height; y++) {
            for (int x = 0; x < ctx.width; x++) {
                if (grid[y * ctx.width + x] == -2) {
                    if (ctx.grid[y * ctx.width + x]) {
                        number_of_full++;
                    } else {
                        number_of_empty++;
                    }
                }
            }
        }
    }

    /*
     * Now go through our sorted list until we find either `number_of_full'
     * empty squares, or `number_of_empty' full squares; these will be
     * swapped with the appropriate squares in the set to either
     * fill or empty the set while keeping the same number of mines
     * overall.
     */
    if (mask) {
        to_fill.reserve(9);
        to_empty.reserve(9);
    } else {
        to_fill.reserve(ctx.size);
        to_empty.reserve(ctx.size);
    }
    for (auto &square: square_list) {
        if (ctx.grid[square.y * ctx.width + square.x]) {
            to_empty.push_back(square.id);
        } else {
            to_fill.push_back(square.id);
        }
        if (to_fill.size() == number_of_full || to_empty.size() == number_of_empty) {
            break;
        }
    }

    /*
     * If we haven't found enough empty squares outside the set to
     * empty it into _or_ enough full squares outside it to fill it
     * up with, we'll have to settle for doing only a partial job.
     * In this case we choose to always _fill_ the set (because
     * this case will tend to crop up when we're working with very
     * high mine densities and the only way to get a solvable grid
     * is going to be to pack most of the mines solidly around the
     * edges). So now our job is to make a list of the empty
     * squares in the set, and shuffle that list so that we fill a
     * random selection of them.
     */
    if (to_fill.size() != number_of_full && to_empty.size() != number_of_empty) {
        assert(to_empty.empty());
        set_list.reserve(ctx.size);

        if (mask) {
            for (int dy = 0; dy < 3; dy++) {
                for (int dx = 0; dx < 3; dx++) {
                    if (mask & (1 << (dy * 3 + dx))) {
                        assert(setx + dx <= ctx.width);
                        assert(sety + dy <= ctx.height);
                        if (!ctx.grid[(sety + dy) * ctx.width + (setx + dx)]) {
                            const std::size_t id = (sety + dy) * ctx.width + (setx + dx);
                            set_list.push_back(id);
                        }
                    }
                }
            }
        } else {
            for (int y = 0; y < ctx.height; y++) {
                for (int x = 0; x < ctx.width; x++) {
                    if (grid[y * ctx.width + x] == -2) {
                        if (!ctx.grid[y * ctx.width + x]) {
                            const std::size_t id = y * ctx.width + x;
                            set_list.push_back(id);
                        }
                    }
                }
            }
        }
        assert(set_list.size() > to_empty.size());

        std::shuffle(set_list.begin(), set_list.end(), ctx.random);
    } else {
        set_list.clear();
    }

    /*
     * Now we're pretty much there. We need to either
     * 	(a) put a mine in each of the empty squares in the set, and
     * 	    take one out of each square in `to_empty'
     * 	(b) take a mine out of each of the full squares in the set,
     * 	    and put one in each square in `to_fill'
     * depending on which one we've found enough squares to do.
     *
     * So we start by constructing our list of changes to return to
     * the solver, so that it can update its data structures
     * efficiently rather than having to rescan the whole grid.
     */
    const std::vector<std::size_t> &todo_list =
            (to_fill.size() == number_of_full) ? to_fill : to_empty;

    int delta_todo, delta_set;
    int index;

    if (to_fill.size() == number_of_full) {
        delta_todo = +1;
        delta_set = -1;
    } else {
        // We also fall into this case if we've constructed a set_list.
        delta_todo = -1;
        delta_set = +1;
    }
    changes.reserve(2 * todo_list.size());
    for (index = 0; index < todo_list.size(); index++) {
        const auto id = todo_list[index];
        const auto pert = perturbation{
                .x = square_list[id].x,
                .y = square_list[id].y,
                .delta = delta_todo
        };
        changes[index] = pert;
    }

    // now index == todo_list.size()

    if (!set_list.empty()) {
        assert(todo_list == to_empty);
        for (auto set_item: set_list) {
            const auto pert = perturbation{
                    .x = set_item % ctx.width,
                    .y = set_item / ctx.width,
                    .delta = delta_set
            };
            changes[index] = pert;
            index++;
        }
    } else if (mask) {
        for (int dy = 0; dy < 3; dy++) {
            for (int dx = 0; dx < 3; dx++) {
                if (mask & (1 << (dy * 3 + dx))) {
                    int current_value = (ctx.grid[(sety + dy) * ctx.width + (setx + dx)] ? +1 : -1);
                    if (delta_set == -current_value) {
                        const auto pert = perturbation{
                                .x = static_cast<size_t>(setx + dx),
                                .y = static_cast<size_t>(sety + dy),
                                .delta = delta_set
                        };
                        changes[index] = pert;
                        index++;
                    }
                }
            }
        }
    } else {
        for (std::size_t y = 0; y < ctx.height; y++) {
            for (std::size_t x = 0; x < ctx.width; x++) {
                if (grid[y * ctx.width + x] == -2) {
                    int current_value = (ctx.grid[y * ctx.width + x] ? +1 : -1);
                    if (delta_set == -current_value) {
                        const auto pert = perturbation{
                                .x = x,
                                .y = y,
                                .delta = delta_set
                        };
                        changes[index] = pert;
                        index++;
                    }
                }
            }
        }
    }

    // Having set up the precise list of changes we're going to make,
    // we now simply make them and return.
    for (auto &change: changes) {
        std::size_t x = change.x;
        std::size_t y = change.y;
        int delta = change.delta;

        // Check we're not trying to add an existing mine or remove an absent one.
        assert((delta < 0) ^ (ctx.grid[y * ctx.width + x] == 0));

        // Actually make the change.
        ctx.grid[y * ctx.width + x] = (delta > 0);

        // Update any numbers already present in the grid.
        for (int dy = -1; dy <= +1; dy++) {
            for (int dx = -1; dx <= +1; dx++) {
                if (x + dx >= 0 && x + dx < ctx.width &&
                    y + dy >= 0 && y + dy < ctx.height &&
                    grid[(y + dy) * ctx.width + (x + dx)] != -2) {
                    if (dx == 0 && dy == 0) {
                        /*
                         * The square itself is marked as known in
                         * the grid. Mark it as a mine if it's a
                         * mine, or else work out its number.
                         */
                        if (delta > 0) {
                            grid[y * ctx.width + x] = -1;
                        } else {
                            std::int8_t mine_count = 0;
                            for (int dy2 = -1; dy2 <= +1; dy2++) {
                                for (int dx2 = -1; dx2 <= +1; dx2++) {
                                    if (x + dx2 >= 0 && x + dx2 < ctx.width &&
                                        y + dy2 >= 0 && y + dy2 < ctx.height &&
                                        ctx.grid[(y + dy2) * ctx.width + (x + dx2)]) {
                                        mine_count++;
                                    }
                                }
                            }
                            grid[y * ctx.width + x] = mine_count;
                        }
                    } else if (grid[(y + dy) * ctx.width + (x + dx)] >= 0) {
                        grid[(y + dy) * ctx.width + (x + dx)] += delta;
                    }
                }
            }
        }
    }
}

static void ss_add_todo(set_store &ss, struct set *s) {
    if (s->todo)
        return;                   /* already on it */

    s->prev = ss.todo_tail;
    if (s->prev)
        s->prev->next = s;
    else
        ss.todo_head = s;
    ss.todo_tail = s;
    s->next = nullptr;
    s->todo = true;
}

static void ss_add(set_store &ss, int x, int y, int mask, int mines) {
    struct set *s;

    assert(mask != 0);

    /*
     * Normalise so that x and y are genuinely the bounding
     * rectangle.
     */
    while (!(mask & (1 | 8 | 64)))
        mask >>= 1, x++;
    while (!(mask & (1 | 2 | 4)))
        mask >>= 3, y++;

    /*
     * Create a set structure and add it to the tree.
     */
    s = snew(struct set);
    s->x = x;
    s->y = y;
    s->mask = mask;
    s->mines = mines;
    s->todo = false;
    if (add234(ss.sets, s) != s) {
        /*
         * This set already existed! Free it and return.
         */
        sfree(s);
        return;
    }

    /*
     * We've added a new set to the tree, so put it on the todo
     * list.
     */
    ss_add_todo(ss, s);
}

static int setmunge(int x1, int y1, int mask1, int x2, int y2, int mask2,
                    bool diff) {
    /*
     * Adjust the second set so that it has the same x,y
     * coordinates as the first.
     */
    if (abs(x2 - x1) >= 3 || abs(y2 - y1) >= 3) {
        mask2 = 0;
    } else {
        while (x2 > x1) {
            mask2 &= ~(4 | 32 | 256);
            mask2 <<= 1;
            x2--;
        }
        while (x2 < x1) {
            mask2 &= ~(1 | 8 | 64);
            mask2 >>= 1;
            x2++;
        }
        while (y2 > y1) {
            mask2 &= ~(64 | 128 | 256);
            mask2 <<= 3;
            y2--;
        }
        while (y2 < y1) {
            mask2 &= ~(1 | 2 | 4);
            mask2 >>= 3;
            y2++;
        }
    }

    /*
     * Invert the second set if `diff' is set (we're after A &~ B
     * rather than A & B).
     */
    if (diff)
        mask2 ^= 511;

    /*
     * Now all that's left is a logical AND.
     */
    return mask1 & mask2;
}

static struct set **ss_overlap(set_store &ss, int x, int y, int mask) {
    struct set **ret = nullptr;
    int nret = 0, retsize = 0;
    int xx, yy;

    for (xx = x - 3; xx < x + 3; xx++)
        for (yy = y - 3; yy < y + 3; yy++) {
            struct set stmp, *s;
            int pos;

            /*
             * Find the first set with these top left coordinates.
             */
            stmp.x = xx;
            stmp.y = yy;
            stmp.mask = 0;

            if (findrelpos234(ss.sets, &stmp, nullptr, REL234_GE, &pos)) {
                while ((s = (set *) index234(ss.sets, pos)) != nullptr &&
                       s->x == xx && s->y == yy) {
                    /*
                     * This set potentially overlaps the input one.
                     * Compute the intersection to see if they
                     * really overlap, and add it to the list if
                     * so.
                     */
                    if (setmunge(x, y, mask, s->x, s->y, s->mask, false)) {
                        /*
                         * There's an overlap.
                         */
                        if (nret >= retsize) {
                            retsize = nret + 32;
                            ret = sresize(ret, retsize, struct set *);
                        }
                        ret[nret++] = s;
                    }

                    pos++;
                }
            }
        }

    ret = sresize(ret, nret + 1, struct set *);
    ret[nret] = nullptr;

    return ret;
}

static struct set *ss_todo(set_store &ss) {
    if (ss.todo_head) {
        struct set *ret = ss.todo_head;
        ss.todo_head = ret->next;
        if (ss.todo_head)
            ss.todo_head->prev = nullptr;
        else
            ss.todo_tail = nullptr;
        ret->next = ret->prev = nullptr;
        ret->todo = false;
        return ret;
    } else {
        return nullptr;
    }
}

static void ss_remove(set_store &ss, struct set *s) {
    struct set *next = s->next, *prev = s->prev;

    /*
     * Remove s from the todo list.
     */
    if (prev)
        prev->next = next;
    else if (s == ss.todo_head)
        ss.todo_head = next;

    if (next)
        next->prev = prev;
    else if (s == ss.todo_tail)
        ss.todo_tail = prev;

    s->todo = false;

    /*
     * Remove s from the tree.
     */
    del234(ss.sets, s);

    /*
     * Destroy the actual set structure.
     */
    sfree(s);
}

static int bitcount16(int inword) {
    unsigned int word = inword;

    word = ((word & 0xAAAA) >> 1) + (word & 0x5555);
    word = ((word & 0xCCCC) >> 2) + (word & 0x3333);
    word = ((word & 0xF0F0) >> 4) + (word & 0x0F0F);
    word = ((word & 0xFF00) >> 8) + (word & 0x00FF);

    return (int) word;
}

void known_squares(int w, int h, std::list<std::size_t> &square_todo,
                   std::basic_string<std::int8_t> &grid,
                   const open_function &open, mine_context &openctx,
                   int x, int y, int mask, bool mine) {
    int xx, yy, bit;

    bit = 1;

    for (yy = 0; yy < 3; yy++)
        for (xx = 0; xx < 3; xx++) {
            if (mask & bit) {
                int i = (y + yy) * w + (x + xx);

                /*
                 * It's possible that this square is _already_
                 * known, in which case we don't try to add it to
                 * the list twice.
                 */
                if (grid[i] == -2) {

                    if (mine) {
                        grid[i] = -1;   /* and don't open it! */
                    } else {
                        grid[i] = open(openctx, x + xx, y + yy);
                        assert(grid[i] != -1);   /* *bang* */
                    }

                    square_todo.push_back(i);
                }
            }
            bit <<= 1;
        }
}

int solve_minefield(
        mine_context &context,
        std::basic_string<std::int8_t> &grid,
        const open_function &open,
        const perturbation_function &perturb,
        std::mt19937 &random
) {
    set_store ss{
            .sets = newtree234(set_comparator),
            .todo_head = nullptr,
            .todo_tail = nullptr
    };

    struct set **list;
    int i, j;
    int nperturbs = 0;

    std::list<std::size_t> square_todo;

    // Initialise that list with all known squares in the input grid.
    for (std::size_t y = 0; y < context.height; y++) {
        for (std::size_t x = 0; x < context.width; x++) {
            std::size_t index = y * context.width + x;
            if (grid[index] != -2) {
                square_todo.push_back(index);
            }
        }
    }

    // Main deductive loop.
    while (true) {
        bool done_something = false;
        struct set *s;

        // If there are any known squares on the todo list, process
        // them and construct a set for each.
        while (!square_todo.empty()) {
            std::size_t index = square_todo.front();
            square_todo.pop_front();

            std::size_t x = index % context.width;
            std::size_t y = index / context.width;

            if (grid[index] >= 0) {
                // Empty square. Construct the set of non-known squares
                // around this one, and determine its mine count.
                std::int8_t mines = grid[index];
                int bit = 1;
                int val = 0;
                for (int dy = -1; dy <= +1; dy++) {
                    for (int dx = -1; dx <= +1; dx++) {
                        if (x + dx < 0 || x + dx >= context.width || y + dy < 0 ||
                            y + dy >= context.height) {
                            /* ignore this one */;
                        } else if (grid[index + dy * context.width + dx] == -1) {
                            mines--;
                        } else if (grid[index + dy * context.width + dx] == -2) {
                            val |= bit;
                        }
                        bit <<= 1;
                    }
                }

                if (val) {
                    ss_add(ss, x - 1, y - 1, val, mines);
                }
            }

            // Now, whether the square is empty or full, we must
            // find any set which contains it and replace it with
            // one which does not.
            {
                list = ss_overlap(ss, x, y, 1);

                for (j = 0; list[j]; j++) {
                    int newmask, newmines;

                    s = list[j];

                    /*
                     * Compute the mask for this set minus the
                     * newly known square.
                     */
                    newmask = setmunge(s->x, s->y, s->mask, x, y, 1, true);

                    /*
                     * Compute the new mine count.
                     */
                    newmines = s->mines - (grid[index] == -1);

                    /*
                     * Insert the new set into the collection,
                     * unless it's been whittled right down to
                     * nothing.
                     */
                    if (newmask)
                        ss_add(ss, s->x, s->y, newmask, newmines);

                    /*
                     * Destroy the old one; it is actually obsolete.
                     */
                    ss_remove(ss, s);
                }

                sfree(list);
            }

            /*
             * Marking a fresh square as known certainly counts as
             * doing something.
             */
            done_something = true;
        }

        /*
         * Now pick a set off the to-do list and attempt deductions
         * based on it.
         */
        if ((s = ss_todo(ss)) != nullptr) {
            /*
             * Firstly, see if this set has a mine count of zero or
             * of its own cardinality.
             */
            if (s->mines == 0 || s->mines == bitcount16(s->mask)) {
                /*
                 * If so, we can immediately mark all the squares
                 * in the set as known.
                 */
                known_squares(context.width, context.height, square_todo, grid, open, context,
                              s->x, s->y, s->mask, (s->mines != 0));

                /*
                 * Having done that, we need do nothing further
                 * with this set; marking all the squares in it as
                 * known will eventually eliminate it, and will
                 * also permit further deductions about anything
                 * that overlaps it.
                 */
                continue;
            }

            /*
             * Failing that, we now search through all the sets
             * which overlap this one.
             */
            list = ss_overlap(ss, s->x, s->y, s->mask);

            for (j = 0; list[j]; j++) {
                struct set *s2 = list[j];
                int swing, s2wing, swc, s2wc;

                /*
                 * Find the non-overlapping parts s2-s and s-s2,
                 * and their cardinalities.
                 *
                 * I'm going to refer to these parts as `wings'
                 * surrounding the central part common to both
                 * sets. The `s wing' is s-s2; the `s2 wing' is
                 * s2-s.
                 */
                swing = setmunge(s->x, s->y, s->mask, s2->x, s2->y, s2->mask,
                                 true);
                s2wing = setmunge(s2->x, s2->y, s2->mask, s->x, s->y, s->mask,
                                  true);
                swc = bitcount16(swing);
                s2wc = bitcount16(s2wing);

                /*
                 * If one set has more mines than the other, and
                 * the number of extra mines is equal to the
                 * cardinality of that set's wing, then we can mark
                 * every square in the wing as a known mine, and
                 * every square in the other wing as known clear.
                 */
                if (swc == s->mines - s2->mines ||
                    s2wc == s2->mines - s->mines) {
                    known_squares(context.width, context.height, square_todo, grid, open, context,
                                  s->x, s->y, swing,
                                  (swc == s->mines - s2->mines));
                    known_squares(context.width, context.height, square_todo, grid, open, context,
                                  s2->x, s2->y, s2wing,
                                  (s2wc == s2->mines - s->mines));
                    continue;
                }

                /*
                 * Failing that, see if one set is a subset of the
                 * other. If so, we can divide up the mine count of
                 * the larger set between the smaller set and its
                 * complement, even if neither smaller set ends up
                 * being immediately clearable.
                 */
                if (swc == 0 && s2wc != 0) {
                    /* s is a subset of s2. */
                    assert(s2->mines > s->mines);
                    ss_add(ss, s2->x, s2->y, s2wing, s2->mines - s->mines);
                } else if (s2wc == 0 && swc != 0) {
                    /* s2 is a subset of s. */
                    assert(s->mines > s2->mines);
                    ss_add(ss, s->x, s->y, swing, s->mines - s2->mines);
                }
            }

            sfree(list);

            /*
             * In this situation we have definitely done
             * _something_, even if it's only reducing the size of
             * our to-do list.
             */
            done_something = true;
        } else if (context.mines >= 0) {
            /*
             * We have nothing left on our todo list, which means
             * all localised deductions have failed. Our next step
             * is to resort to global deduction based on the total
             * mine count. This is computationally expensive
             * compared to any of the above deductions, which is
             * why we only ever do it when all else fails, so that
             * hopefully it won't have to happen too often.
             *
             * If you pass n<0 into this solver, that informs it
             * that you do not know the total mine count, so it
             * won't even attempt these deductions.
             */

            int minesleft, squaresleft;
            int nsets, cursor;
            bool setused[10];

            /*
             * Start by scanning the current grid state to work out
             * how many unknown squares we still have, and how many
             * mines are to be placed in them.
             */
            squaresleft = 0;
            minesleft = context.mines;
            for (i = 0; i < context.size; i++) {
                if (grid[i] == -1)
                    minesleft--;
                else if (grid[i] == -2)
                    squaresleft++;
            }

            /*
             * If there _are_ no unknown squares, we have actually
             * finished.
             */
            if (squaresleft == 0) {
                assert(minesleft == 0);
                break;
            }

            /*
             * First really simple case: if there are no more mines
             * left, or if there are exactly as many mines left as
             * squares to play them in, then it's all easy.
             */
            if (minesleft == 0 || minesleft == squaresleft) {
                for (i = 0; i < context.size; i++)
                    if (grid[i] == -2)
                        known_squares(context.width, context.height, square_todo, grid, open,
                                      context,
                                      i % context.width, i / context.width, 1, minesleft != 0);
                continue;           /* now go back to main deductive loop */
            }

            /*
             * Failing that, we have to do some _real_ work.
             * Ideally what we do here is to try every single
             * combination of the currently available sets, in an
             * attempt to find a disjoint union (i.e. a set of
             * squares with a known mine count between them) such
             * that the remaining unknown squares _not_ contained
             * in that union either contain no mines or are all
             * mines.
             *
             * Actually enumerating all 2^n possibilities will get
             * a bit slow for large n, so I artificially cap this
             * recursion at n=10 to avoid too much pain.
             */
            nsets = count234(ss.sets);
            if (nsets <= lenof(setused)) {
                /*
                 * Doing this with actual recursive function calls
                 * would get fiddly because a load of local
                 * variables from this function would have to be
                 * passed down through the recursion. So instead
                 * I'm going to use a virtual recursion within this
                 * function. The way this works is:
                 *
                 *  - we have an array `setused', such that setused[n]
                 *    is true if set n is currently in the union we
                 *    are considering.
                 *
                 *  - we have a value `cursor' which indicates how
                 *    much of `setused' we have so far filled in.
                 *    It's conceptually the recursion depth.
                 *
                 * We begin by setting `cursor' to zero. Then:
                 *
                 *  - if cursor can advance, we advance it by one. We
                 *    set the value in `setused' that it went past to
                 *    true if that set is disjoint from anything else
                 *    currently in `setused', or to false otherwise.
                 *
                 *  - If cursor cannot advance because it has
                 *    reached the end of the setused list, then we
                 *    have a maximal disjoint union. Check to see
                 *    whether its mine count has any useful
                 *    properties. If so, mark all the squares not
                 *    in the union as known and terminate.
                 *
                 *  - If cursor has reached the end of setused and the
                 *    algorithm _hasn't_ terminated, back cursor up to
                 *    the nearest true entry, reset it to false, and
                 *    advance cursor just past it.
                 *
                 *  - If we attempt to back up to the nearest 1 and
                 *    there isn't one at all, then we have gone
                 *    through all disjoint unions of sets in the
                 *    list and none of them has been helpful, so we
                 *    give up.
                 */
                struct set *sets[lenof(setused)];
                for (i = 0; i < nsets; i++)
                    sets[i] = static_cast<set *>(index234(ss.sets, i));

                cursor = 0;
                while (true) {

                    if (cursor < nsets) {
                        bool ok = true;

                        /* See if any existing set overlaps this one. */
                        for (i = 0; i < cursor; i++)
                            if (setused[i] &&
                                setmunge(sets[cursor]->x,
                                         sets[cursor]->y,
                                         sets[cursor]->mask,
                                         sets[i]->x, sets[i]->y, sets[i]->mask,
                                         false)) {
                                ok = false;
                                break;
                            }

                        if (ok) {
                            /*
                             * We're adding this set to our union,
                             * so adjust minesleft and squaresleft
                             * appropriately.
                             */
                            minesleft -= sets[cursor]->mines;
                            squaresleft -= bitcount16(sets[cursor]->mask);
                        }

                        setused[cursor++] = ok;
                    } else {
                        /*
                         * We've reached the end. See if we've got
                         * anything interesting.
                         */
                        if (squaresleft > 0 &&
                            (minesleft == 0 || minesleft == squaresleft)) {
                            /*
                             * We have! There is at least one
                             * square not contained within the set
                             * union we've just found, and we can
                             * deduce that either all such squares
                             * are mines or all are not (depending
                             * on whether minesleft==0). So now all
                             * we have to do is actually go through
                             * the grid, find those squares, and
                             * mark them.
                             */
                            for (std::size_t i = 0; i < context.size; i++)
                                if (grid[i] == -2) {
                                    bool outside = true;
                                    std::size_t y = i / context.width;
                                    std::size_t x = i % context.width;
                                    for (j = 0; j < nsets; j++)
                                        if (setused[j] &&
                                            setmunge(sets[j]->x, sets[j]->y,
                                                     sets[j]->mask, x, y, 1,
                                                     false)) {
                                            outside = false;
                                            break;
                                        }
                                    if (outside)
                                        known_squares(context.width, context.height, square_todo,
                                                      grid,
                                                      open, context,
                                                      x, y, 1, minesleft != 0);
                                }

                            done_something = true;
                            break;     /* return to main deductive loop */
                        }

                        /*
                         * If we reach here, then this union hasn't
                         * done us any good, so move on to the
                         * next. Backtrack cursor to the nearest 1,
                         * change it to a 0 and continue.
                         */
                        while (--cursor >= 0 && !setused[cursor]);
                        if (cursor >= 0) {
                            assert(setused[cursor]);

                            /*
                             * We're removing this set from our
                             * union, so re-increment minesleft and
                             * squaresleft.
                             */
                            minesleft += sets[cursor]->mines;
                            squaresleft += bitcount16(sets[cursor]->mask);

                            setused[cursor++] = false;
                        } else {
                            /*
                             * We've backtracked all the way to the
                             * start without finding a single 1,
                             * which means that our virtual
                             * recursion is complete and nothing
                             * helped.
                             */
                            break;
                        }
                    }

                }

            }
        }

        if (done_something)
            continue;

        /*
         * Now we really are at our wits' end as far as solving
         * this grid goes. Our only remaining option is to call
         * a perturb function and ask it to modify the grid to
         * make it easier.
         */
        if (perturb) {
            struct set *s;

            nperturbs++;

            /*
             * Choose a set at random from the current selection,
             * and ask the perturb function to either fill or empty
             * it.
             *
             * If we have no sets at all, we must give up.
             */
            if (count234(ss.sets) == 0) {
                perturb(context, grid, 0, 0, 0);
            } else {
                s = static_cast<set *>(index234(ss.sets,
                                                random_up_to(random, count234(ss.sets))));
                perturb(context, grid, s->x, s->y, s->mask);
            }

            if (!context.changes.empty()) {
                /*
                 * A number of squares have been fiddled with, and
                 * the returned structure tells us which. Adjust
                 * the mine count in any set which overlaps one of
                 * those squares, and put them back on the to-do
                 * list. Also, if the square itself is marked as a
                 * known non-mine, put it back on the squares-to-do
                 * list.
                 */
                const std::vector<perturbation>& result = context.changes;
                for (i = 0; i < result.size(); i++) {
                    if (result[i].delta < 0 &&
                        grid[result[i].y * context.width + result[i].x] != -2) {
                        auto index = result[i].y * context.width + result[i].x;
                        square_todo.push_back(index);
                    }

                    list = ss_overlap(ss, result[i].x, result[i].y, 1);

                    for (j = 0; list[j]; j++) {
                        list[j]->mines += result[i].delta;
                        ss_add_todo(ss, list[j]);
                    }

                    sfree(list);
                }

                /*
                 * And now we can go back round the deductive loop.
                 */
                continue;
            }
        }

        /*
         * If we get here, even that didn't work (either we didn't
         * have a perturb function or it returned failure), so we
         * give up entirely.
         */
        break;
    }

    /*
     * See if we've got any unknown squares left.
     */
    for (std::size_t y = 0; y < context.height; y++) {
        for (std::size_t x = 0; x < context.width; x++) {
            if (grid[y * context.width + x] == -2) {
                nperturbs = -1;           /* failed to complete */
                break;
            }
        }
    }

    /*
     * Free the set list and square-todo list.
     */
    {
        struct set *s;
        while ((s = static_cast<set *>(delpos234(ss.sets, 0))) != nullptr)
            sfree(s);
        freetree234(ss.sets);
    }

    return nperturbs;
}

bool try_solve_minefield(mine_context &context, std::mt19937 &random) {
    bool result;

    /*
     * Now set up a results grid to run the solver in, and a
     * mine_context for the solver to open squares. Then run the solver
     * repeatedly; if the number of perturb steps ever goes up or
     * it ever returns -1, give up completely.
     *
     * We bypass this bit if we're not after a unique grid.
     */

    std::basic_string<std::int8_t> solve_grid(context.size, -2);

    while (true) {
        std::fill(solve_grid.begin(), solve_grid.end(), -2);

        solve_grid[context.start_y * context.width + context.start_x] = mine_open(context,
                                                                                  context.start_x,
                                                                                  context.start_y);
        assert(solve_grid[context.start_y * context.width + context.start_x] ==
               0); /* by deliberate arrangement */

        int solve_result =
                solve_minefield(context, solve_grid, mine_open,
                                mine_perturbation,
                                random);
        if (solve_result < 0) {
            result = false;
            break;
        } else if (solve_result == 0) {
            result = true;
            break;
        }
    }

    return result;
}

extern "C" JNIEXPORT jstring JNICALL
Java_dev_lucasnlm_antimine_sgtatham_SgTathamMines_createMinefield(
        JNIEnv *env,
        jobject javaThis,
        jlong inSeed,
        jint slice_width,
        jint inWidth,
        jint inHeight,
        jint inMines,
        jint inX,
        jint inY
) {
    std::mt19937 random(inSeed);
    std::string minefield = new_mine_layout(slice_width, inWidth, inHeight, inMines, inX, inY, random);
    return env->NewStringUTF(minefield.c_str());
}