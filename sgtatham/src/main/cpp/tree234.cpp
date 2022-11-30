#include <cstdio>
#include <cstdlib>
#include <cassert>
#include <iostream>

#include "tree234.h"

typedef struct node234_Tag node234;

struct tree234_Tag {
    node234 *root;
    cmpfn234 cmp;
};

struct node234_Tag {
    node234 *parent;
    node234 *kids[4];
    int counts[4];
    void *elems[3];
};

/*
 * Create a 2-3-4 tree.
 */
tree234 *newtree234(cmpfn234 cmp) {
    tree234 *ret = snew(tree234);
    ret->root = nullptr;
    ret->cmp = cmp;
    return ret;
}

/*
 * Free a 2-3-4 tree (not including freeing the elements).
 */
static void freenode234(node234 *n) {
    if (!n)
        return;
    freenode234(n->kids[0]);
    freenode234(n->kids[1]);
    freenode234(n->kids[2]);
    freenode234(n->kids[3]);
    sfree(n);
}
void freetree234(tree234 *t) {
    freenode234(t->root);
    sfree(t);
}

/*
 * Internal function to count a node.
 */
static int countnode234(node234 *n) {
    int count = 0;
    int i;
    if (!n)
        return 0;
    for (i = 0; i < 4; i++)
        count += n->counts[i];
    for (i = 0; i < 3; i++)
        if (n->elems[i])
            count++;
    return count;
}

/*
 * Count the elements in a tree.
 */
int count234(tree234 *t) {
    if (t->root)
        return countnode234(t->root);
    else
        return 0;
}

/*
 * Propagate a node overflow up a tree until it stops. Returns 0 or
 * 1, depending on whether the root had to be split or not.
 */
static int add234_insert(node234 *left, void *e, node234 *right,
                         node234 **root, node234 *n, int ki) {
    int lcount, rcount;
    /*
     * We need to insert the new left/element/right set in n at
     * child position ki.
     */
    lcount = countnode234(left);
    rcount = countnode234(right);
    while (n) {
        if (n->elems[1] == nullptr) {
            /*
             * Insert in a 2-node; simple.
             */
            if (ki == 0) {
                n->kids[2] = n->kids[1];     n->counts[2] = n->counts[1];
                n->elems[1] = n->elems[0];
                n->kids[1] = right;          n->counts[1] = rcount;
                n->elems[0] = e;
                n->kids[0] = left;           n->counts[0] = lcount;
            } else { /* ki == 1 */
                n->kids[2] = right;          n->counts[2] = rcount;
                n->elems[1] = e;
                n->kids[1] = left;           n->counts[1] = lcount;
            }
            if (n->kids[0]) n->kids[0]->parent = n;
            if (n->kids[1]) n->kids[1]->parent = n;
            if (n->kids[2]) n->kids[2]->parent = n;
            break;
        } else if (n->elems[2] == nullptr) {
            /*
             * Insert in a 3-node; simple.
             */
            if (ki == 0) {
                n->kids[3] = n->kids[2];    n->counts[3] = n->counts[2];
                n->elems[2] = n->elems[1];
                n->kids[2] = n->kids[1];    n->counts[2] = n->counts[1];
                n->elems[1] = n->elems[0];
                n->kids[1] = right;         n->counts[1] = rcount;
                n->elems[0] = e;
                n->kids[0] = left;          n->counts[0] = lcount;
            } else if (ki == 1) {
                n->kids[3] = n->kids[2];    n->counts[3] = n->counts[2];
                n->elems[2] = n->elems[1];
                n->kids[2] = right;         n->counts[2] = rcount;
                n->elems[1] = e;
                n->kids[1] = left;          n->counts[1] = lcount;
            } else { /* ki == 2 */
                n->kids[3] = right;         n->counts[3] = rcount;
                n->elems[2] = e;
                n->kids[2] = left;          n->counts[2] = lcount;
            }
            if (n->kids[0]) n->kids[0]->parent = n;
            if (n->kids[1]) n->kids[1]->parent = n;
            if (n->kids[2]) n->kids[2]->parent = n;
            if (n->kids[3]) n->kids[3]->parent = n;
            break;
        } else {
            node234 *m = snew(node234);
            m->parent = n->parent;
            /*
             * Insert in a 4-node; split into a 2-node and a
             * 3-node, and move focus up a level.
             *
             * I don't think it matters which way round we put the
             * 2 and the 3. For simplicity, we'll put the 3 first
             * always.
             */
            if (ki == 0) {
                m->kids[0] = left;          m->counts[0] = lcount;
                m->elems[0] = e;
                m->kids[1] = right;         m->counts[1] = rcount;
                m->elems[1] = n->elems[0];
                m->kids[2] = n->kids[1];    m->counts[2] = n->counts[1];
                e = n->elems[1];
                n->kids[0] = n->kids[2];    n->counts[0] = n->counts[2];
                n->elems[0] = n->elems[2];
                n->kids[1] = n->kids[3];    n->counts[1] = n->counts[3];
            } else if (ki == 1) {
                m->kids[0] = n->kids[0];    m->counts[0] = n->counts[0];
                m->elems[0] = n->elems[0];
                m->kids[1] = left;          m->counts[1] = lcount;
                m->elems[1] = e;
                m->kids[2] = right;         m->counts[2] = rcount;
                e = n->elems[1];
                n->kids[0] = n->kids[2];    n->counts[0] = n->counts[2];
                n->elems[0] = n->elems[2];
                n->kids[1] = n->kids[3];    n->counts[1] = n->counts[3];
            } else if (ki == 2) {
                m->kids[0] = n->kids[0];    m->counts[0] = n->counts[0];
                m->elems[0] = n->elems[0];
                m->kids[1] = n->kids[1];    m->counts[1] = n->counts[1];
                m->elems[1] = n->elems[1];
                m->kids[2] = left;          m->counts[2] = lcount;
                /* e = e; */
                n->kids[0] = right;         n->counts[0] = rcount;
                n->elems[0] = n->elems[2];
                n->kids[1] = n->kids[3];    n->counts[1] = n->counts[3];
            } else { /* ki == 3 */
                m->kids[0] = n->kids[0];    m->counts[0] = n->counts[0];
                m->elems[0] = n->elems[0];
                m->kids[1] = n->kids[1];    m->counts[1] = n->counts[1];
                m->elems[1] = n->elems[1];
                m->kids[2] = n->kids[2];    m->counts[2] = n->counts[2];
                n->kids[0] = left;          n->counts[0] = lcount;
                n->elems[0] = e;
                n->kids[1] = right;         n->counts[1] = rcount;
                e = n->elems[2];
            }
            m->kids[3] = n->kids[3] = n->kids[2] = nullptr;
            m->counts[3] = n->counts[3] = n->counts[2] = 0;
            m->elems[2] = n->elems[2] = n->elems[1] = nullptr;
            if (m->kids[0]) m->kids[0]->parent = m;
            if (m->kids[1]) m->kids[1]->parent = m;
            if (m->kids[2]) m->kids[2]->parent = m;
            if (n->kids[0]) n->kids[0]->parent = n;
            if (n->kids[1]) n->kids[1]->parent = n;
            left = m;  lcount = countnode234(left);
            right = n; rcount = countnode234(right);
        }
        if (n->parent)
            ki = (n->parent->kids[0] == n ? 0 :
                  n->parent->kids[1] == n ? 1 :
                  n->parent->kids[2] == n ? 2 : 3);
        n = n->parent;
    }

    /*
     * If we've come out of here by `break', n will still be
     * non-nullptr and all we need to do is go back up the tree
     * updating counts. If we've come here because n is nullptr, we
     * need to create a new root for the tree because the old one
     * has just split into two. */
    if (n) {
        while (n->parent) {
            int count = countnode234(n);
            int childnum;
            childnum = (n->parent->kids[0] == n ? 0 :
                        n->parent->kids[1] == n ? 1 :
                        n->parent->kids[2] == n ? 2 : 3);
            n->parent->counts[childnum] = count;
            n = n->parent;
        }
        return 0;		       /* root unchanged */
    } else {
        (*root) = snew(node234);
        (*root)->kids[0] = left;     (*root)->counts[0] = lcount;
        (*root)->elems[0] = e;
        (*root)->kids[1] = right;    (*root)->counts[1] = rcount;
        (*root)->elems[1] = nullptr;
        (*root)->kids[2] = nullptr;     (*root)->counts[2] = 0;
        (*root)->elems[2] = nullptr;
        (*root)->kids[3] = nullptr;     (*root)->counts[3] = 0;
        (*root)->parent = nullptr;
        if ((*root)->kids[0]) (*root)->kids[0]->parent = (*root);
        if ((*root)->kids[1]) (*root)->kids[1]->parent = (*root);
        return 1;		       /* root moved */
    }
}

/*
 * Add an element e to a 2-3-4 tree t. Returns e on success, or if
 * an existing element compares equal, returns that.
 */
static void *add234_internal(tree234 *t, void *e, int index) {
    node234 *n;
    int ki;
    void *orig_e = e;
    int c;

    if (t->root == nullptr) {
        t->root = snew(node234);
        t->root->elems[1] = t->root->elems[2] = nullptr;
        t->root->kids[0] = t->root->kids[1] = nullptr;
        t->root->kids[2] = t->root->kids[3] = nullptr;
        t->root->counts[0] = t->root->counts[1] = 0;
        t->root->counts[2] = t->root->counts[3] = 0;
        t->root->parent = nullptr;
        t->root->elems[0] = e;
        return orig_e;
    }

    n = t->root;
    while (n) {
        if (index >= 0) {
            if (!n->kids[0]) {
                /*
                 * Leaf node. We want to insert at kid position
                 * equal to the index:
                 *
                 *   0 A 1 B 2 C 3
                 */
                ki = index;
            } else {
                /*
                 * Internal node. We always descend through it (add
                 * always starts at the bottom, never in the
                 * middle).
                 */
                if (index <= n->counts[0]) {
                    ki = 0;
                } else if (index -= n->counts[0] + 1, index <= n->counts[1]) {
                    ki = 1;
                } else if (index -= n->counts[1] + 1, index <= n->counts[2]) {
                    ki = 2;
                } else if (index -= n->counts[2] + 1, index <= n->counts[3]) {
                    ki = 3;
                } else
                    return nullptr;       /* error: index out of range */
            }
        } else {
            if ((c = t->cmp(e, n->elems[0])) < 0)
                ki = 0;
            else if (c == 0)
                return n->elems[0];	       /* already exists */
            else if (n->elems[1] == nullptr || (c = t->cmp(e, n->elems[1])) < 0)
                ki = 1;
            else if (c == 0)
                return n->elems[1];	       /* already exists */
            else if (n->elems[2] == nullptr || (c = t->cmp(e, n->elems[2])) < 0)
                ki = 2;
            else if (c == 0)
                return n->elems[2];	       /* already exists */
            else
                ki = 3;
        }
        if (!n->kids[ki])
            break;
        n = n->kids[ki];
    }

    add234_insert(nullptr, e, nullptr, &t->root, n, ki);

    return orig_e;
}

void *add234(tree234 *t, void *e) {
    if (!t->cmp)		       /* tree is unsorted */
        return nullptr;

    return add234_internal(t, e, -1);
}

/*
 * Look up the element at a given numeric index in a 2-3-4 tree.
 * Returns nullptr if the index is out of range.
 */
void *index234(tree234 *t, int index) {
    node234 *n;

    if (!t->root)
        return nullptr;		       /* tree is empty */

    if (index < 0 || index >= countnode234(t->root))
        return nullptr;		       /* out of range */

    n = t->root;

    while (n) {
        if (index < n->counts[0])
            n = n->kids[0];
        else if (index -= n->counts[0] + 1, index < 0)
            return n->elems[0];
        else if (index < n->counts[1])
            n = n->kids[1];
        else if (index -= n->counts[1] + 1, index < 0)
            return n->elems[1];
        else if (index < n->counts[2])
            n = n->kids[2];
        else if (index -= n->counts[2] + 1, index < 0)
            return n->elems[2];
        else
            n = n->kids[3];
    }

    /* We shouldn't ever get here. I wonder how we did. */
    return nullptr;
}

/*
 * Find an element e in a sorted 2-3-4 tree t. Returns nullptr if not
 * found. e is always passed as the first argument to cmp, so cmp
 * can be an asymmetric function if desired. cmp can also be passed
 * as nullptr, in which case the compare function from the tree proper
 * will be used.
 */
void *findrelpos234(tree234 *t, void *e, cmpfn234 cmp,
                    int relation, int *index) {
    node234 *n;
    void *ret;
    int c;
    int idx, ecount, kcount, cmpret;

    if (t->root == nullptr)
        return nullptr;

    if (cmp == nullptr)
        cmp = t->cmp;

    n = t->root;
    /*
     * Attempt to find the element itself.
     */
    idx = 0;
    ecount = -1;
    /*
     * Prepare a fake `cmp' result if e is nullptr.
     */
    cmpret = 0;
    if (e == nullptr) {
        assert(relation == REL234_LT || relation == REL234_GT);
        if (relation == REL234_LT)
            cmpret = +1;	       /* e is a max: always greater */
        else if (relation == REL234_GT)
            cmpret = -1;	       /* e is a min: always smaller */
    }
    while (true) {
        for (kcount = 0; kcount < 4; kcount++) {
            if (kcount >= 3 || n->elems[kcount] == nullptr ||
                (c = cmpret ? cmpret : cmp(e, n->elems[kcount])) < 0) {
                break;
            }
            if (n->kids[kcount]) idx += n->counts[kcount];
            if (c == 0) {
                ecount = kcount;
                break;
            }
            idx++;
        }
        if (ecount >= 0)
            break;
        if (n->kids[kcount])
            n = n->kids[kcount];
        else
            break;
    }

    if (ecount >= 0) {
        /*
         * We have found the element we're looking for. It's
         * n->elems[ecount], at tree index idx. If our search
         * relation is EQ, LE or GE we can now go home.
         */
        if (relation != REL234_LT && relation != REL234_GT) {
            if (index) *index = idx;
            return n->elems[ecount];
        }

        /*
         * Otherwise, we'll do an indexed lookup for the previous
         * or next element. (It would be perfectly possible to
         * implement these search types in a non-counted tree by
         * going back up from where we are, but far more fiddly.)
         */
        if (relation == REL234_LT)
            idx--;
        else
            idx++;
    } else {
        /*
         * We've found our way to the bottom of the tree and we
         * know where we would insert this node if we wanted to:
         * we'd put it in in place of the (empty) subtree
         * n->kids[kcount], and it would have index idx
         *
         * But the actual element isn't there. So if our search
         * relation is EQ, we're doomed.
         */
        if (relation == REL234_EQ)
            return nullptr;

        /*
         * Otherwise, we must do an index lookup for index idx-1
         * (if we're going left - LE or LT) or index idx (if we're
         * going right - GE or GT).
         */
        if (relation == REL234_LT || relation == REL234_LE) {
            idx--;
        }
    }

    /*
     * We know the index of the element we want; just call index234
     * to do the rest. This will return nullptr if the index is out of
     * bounds, which is exactly what we want.
     */
    ret = index234(t, idx);
    if (ret && index) *index = idx;
    return ret;
}

/*
 * Tree transformation used in delete and split: move a subtree
 * right, from child ki of a node to the next child. Update k and
 * index so that they still point to the same place in the
 * transformed tree. Assumes the destination child is not full, and
 * that the source child does have a subtree to spare. Can cope if
 * the destination child is undersized.
 *
 *                . C .                     . B .
 *               /     \     ->            /     \
 * [more] a A b B c   d D e      [more] a A b   c C d D e
 *
 *                 . C .                     . B .
 *                /     \    ->             /     \
 *  [more] a A b B c     d        [more] a A b   c C d
 */
static void trans234_subtree_right(node234 *n, int ki, int *k, int *index) {
    node234 *src, *dest;
    int i, srclen, adjust;

    src = n->kids[ki];
    dest = n->kids[ki+1];

    /*
     * Move over the rest of the destination node to make space.
     */
    dest->kids[3] = dest->kids[2];    dest->counts[3] = dest->counts[2];
    dest->elems[2] = dest->elems[1];
    dest->kids[2] = dest->kids[1];    dest->counts[2] = dest->counts[1];
    dest->elems[1] = dest->elems[0];
    dest->kids[1] = dest->kids[0];    dest->counts[1] = dest->counts[0];

    /* which element to move over */
    i = (src->elems[2] ? 2 : src->elems[1] ? 1 : 0);

    dest->elems[0] = n->elems[ki];
    n->elems[ki] = src->elems[i];
    src->elems[i] = nullptr;

    dest->kids[0] = src->kids[i+1];   dest->counts[0] = src->counts[i+1];
    src->kids[i+1] = nullptr;            src->counts[i+1] = 0;

    if (dest->kids[0]) dest->kids[0]->parent = dest;

    adjust = dest->counts[0] + 1;

    n->counts[ki] -= adjust;
    n->counts[ki+1] += adjust;

    srclen = n->counts[ki];

    if (k) {
        if ((*k) == ki && (*index) > srclen) {
            (*index) -= srclen + 1;
            (*k)++;
        } else if ((*k) == ki+1) {
            (*index) += adjust;
        }
    }
}

/*
 * Tree transformation used in delete and split: move a subtree
 * left, from child ki of a node to the previous child. Update k
 * and index so that they still point to the same place in the
 * transformed tree. Assumes the destination child is not full, and
 * that the source child does have a subtree to spare. Can cope if
 * the destination child is undersized.
 *
 *      . B .                             . C .
 *     /     \                ->         /     \
 *  a A b   c C d D e [more]      a A b B c   d D e [more]
 *
 *     . A .                             . B .
 *    /     \                 ->        /     \
 *   a   b B c C d [more]            a A b   c C d [more]
 */
static void trans234_subtree_left(node234 *n, int ki, int *k, int *index) {
    node234 *src, *dest;
    int i, adjust;

    src = n->kids[ki];
    dest = n->kids[ki-1];

    /* where in dest to put it */
    i = (dest->elems[1] ? 2 : dest->elems[0] ? 1 : 0);
    dest->elems[i] = n->elems[ki-1];
    n->elems[ki-1] = src->elems[0];

    dest->kids[i+1] = src->kids[0];   dest->counts[i+1] = src->counts[0];

    if (dest->kids[i+1]) dest->kids[i+1]->parent = dest;

    /*
     * Move over the rest of the source node.
     */
    src->kids[0] = src->kids[1];      src->counts[0] = src->counts[1];
    src->elems[0] = src->elems[1];
    src->kids[1] = src->kids[2];      src->counts[1] = src->counts[2];
    src->elems[1] = src->elems[2];
    src->kids[2] = src->kids[3];      src->counts[2] = src->counts[3];
    src->elems[2] = nullptr;
    src->kids[3] = nullptr;              src->counts[3] = 0;

    adjust = dest->counts[i+1] + 1;

    n->counts[ki] -= adjust;
    n->counts[ki-1] += adjust;

    if (k) {
        if ((*k) == ki) {
            (*index) -= adjust;
            if ((*index) < 0) {
                (*index) += n->counts[ki-1] + 1;
                (*k)--;
            }
        }
    }
}

/*
 * Tree transformation used in delete and split: merge child nodes
 * ki and ki+1 of a node. Update k and index so that they still
 * point to the same place in the transformed tree. Assumes both
 * children _are_ sufficiently small.
 *
 *      . B .                .
 *     /     \     ->        |
 *  a A b   c C d      a A b B c C d
 *
 * This routine can also cope with either child being undersized:
 *
 *     . A .                 .
 *    /     \      ->        |
 *   a     b B c         a A b B c
 *
 *    . A .                  .
 *   /     \       ->        |
 *  a   b B c C d      a A b B c C d
 */
static void trans234_subtree_merge(node234 *n, int ki, int *k, int *index) {
    node234 *left, *right;
    int i, leftlen, rightlen, lsize, rsize;

    left = n->kids[ki];               leftlen = n->counts[ki];
    right = n->kids[ki+1];            rightlen = n->counts[ki+1];

    assert(!left->elems[2] && !right->elems[2]);   /* neither is large! */
    lsize = (left->elems[1] ? 2 : left->elems[0] ? 1 : 0);
    rsize = (right->elems[1] ? 2 : right->elems[0] ? 1 : 0);

    left->elems[lsize] = n->elems[ki];

    for (i = 0; i < rsize+1; i++) {
        left->kids[lsize+1+i] = right->kids[i];
        left->counts[lsize+1+i] = right->counts[i];
        if (left->kids[lsize+1+i])
            left->kids[lsize+1+i]->parent = left;
        if (i < rsize)
            left->elems[lsize+1+i] = right->elems[i];
    }

    n->counts[ki] += rightlen + 1;

    sfree(right);

    /*
     * Move the rest of n up by one.
     */
    for (i = ki+1; i < 3; i++) {
        n->kids[i] = n->kids[i+1];
        n->counts[i] = n->counts[i+1];
    }
    for (i = ki; i < 2; i++) {
        n->elems[i] = n->elems[i+1];
    }
    n->kids[3] = nullptr;
    n->counts[3] = 0;
    n->elems[2] = nullptr;

    if (k) {
        if ((*k) == ki+1) {
            (*k)--;
            (*index) += leftlen + 1;
        } else if ((*k) > ki+1) {
            (*k)--;
        }
    }
}

/*
 * Delete an element e in a 2-3-4 tree. Does not free the element,
 * merely removes all links to it from the tree nodes.
 */
static void *delpos234_internal(tree234 *t, int index) {
    node234 *n;
    void *retval;
    int ki, i;

    retval = nullptr;

    n = t->root;		       /* by assumption this is non-nullptr */
    while (true) {
        node234 *sub;

        if (index <= n->counts[0]) {
            ki = 0;
        } else if (index -= n->counts[0]+1, index <= n->counts[1]) {
            ki = 1;
        } else if (index -= n->counts[1]+1, index <= n->counts[2]) {
            ki = 2;
        } else if (index -= n->counts[2]+1, index <= n->counts[3]) {
            ki = 3;
        } else {
            assert(0);		       /* can't happen */
        }

        if (!n->kids[0])
            break;		       /* n is a leaf node; we're here! */

        /*
         * Check to see if we've found our target element. If so,
         * we must choose a new target (we'll use the old target's
         * successor, which will be in a leaf), move it into the
         * place of the old one, continue down to the leaf and
         * delete the old copy of the new target.
         */
        if (index == n->counts[ki]) {
            node234 *m;
            assert(n->elems[ki]);      /* must be a kid _before_ an element */
            ki++; index = 0;
            for (m = n->kids[ki]; m->kids[0]; m = m->kids[0])
                continue;
            retval = n->elems[ki-1];
            n->elems[ki-1] = m->elems[0];
        }

        /*
         * Recurse down to subtree ki. If it has only one element,
         * we have to do some transformation to start with.
         */
        sub = n->kids[ki];
        if (!sub->elems[1]) {
            if (ki > 0 && n->kids[ki-1]->elems[1]) {
                /*
                 * Child ki has only one element, but child
                 * ki-1 has two or more. So we need to move a
                 * subtree from ki-1 to ki.
                 */
                trans234_subtree_right(n, ki-1, &ki, &index);
            } else if (ki < 3 && n->kids[ki+1] &&
                       n->kids[ki+1]->elems[1]) {
                /*
                 * Child ki has only one element, but ki+1 has
                 * two or more. Move a subtree from ki+1 to ki.
                 */
                trans234_subtree_left(n, ki+1, &ki, &index);
            } else {
                /*
                 * ki is small with only small neighbours. Pick a
                 * neighbour and merge with it.
                 */
                trans234_subtree_merge(n, ki>0 ? ki-1 : ki, &ki, &index);
                sub = n->kids[ki];

                if (!n->elems[0]) {
                    /*
                     * The root is empty and needs to be
                     * removed.
                     */
                    t->root = sub;
                    sub->parent = nullptr;
                    sfree(n);
                    n = nullptr;
                }
            }
        }

        if (n)
            n->counts[ki]--;
        n = sub;
    }

    /*
     * Now n is a leaf node, and ki marks the element number we
     * want to delete. We've already arranged for the leaf to be
     * bigger than minimum size, so let's just go to it.
     */
    assert(!n->kids[0]);
    if (!retval)
        retval = n->elems[ki];

    for (i = ki; i < 2 && n->elems[i+1]; i++)
        n->elems[i] = n->elems[i+1];
    n->elems[i] = nullptr;

    /*
     * It's just possible that we have reduced the leaf to zero
     * size. This can only happen if it was the root - so destroy
     * it and make the tree empty.
     */
    if (!n->elems[0]) {
        assert(n == t->root);
        sfree(n);
        t->root = nullptr;
    }

    return retval;		       /* finished! */
}

void *delpos234(tree234 *t, int index) {
    if (index < 0 || index >= countnode234(t->root))
        return nullptr;
    return delpos234_internal(t, index);
}

void *del234(tree234 *t, void *e) {
    int index;
    if (!findrelpos234(t, e, nullptr, REL234_EQ, &index))
        return nullptr;		       /* it wasn't in there anyway */
    return delpos234_internal(t, index); /* it's there; delete it. */
}

