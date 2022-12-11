#include "minefield_solver.h"
#include "minefield_creator.h"
#include "random_generator.h"
#include "common.h"

std::string new_mine_layout(
        std::size_t width,
        std::size_t height,
        std::size_t mines_amount,
        std::size_t x_position,
        std::size_t y_position,
        std::mt19937 &random
) {
    std::basic_string<std::size_t> grid =
            generate_safe_minefield(width, height, mines_amount, x_position, y_position, random);
    return minefield_to_string(grid);
}

std::string minefield_to_string(const std::basic_string<std::size_t> &grid) {
    std::string result(grid.size(), '0');

    for (int i = 0; i < grid.size(); i++) {
        if (grid[i]) {
            result[i] = '1';
        }
    }

    return result;
}

std::basic_string<std::size_t> generate_safe_minefield(
        std::size_t width,
        std::size_t height,
        std::size_t mines_amount,
        std::size_t start_x,
        std::size_t start_y,
        std::mt19937 &random
) {
    std::basic_string<std::size_t> result;

    bool success;
    int tries = 0;

    do {
        tries++;

        result = generate_random_minefield_with_safe_area(width,
                                                          height,
                                                          mines_amount,
                                                          start_x,
                                                          start_y,
                                                          random);

        mine_context ctx{
                .grid = result,
                .square_list = {},
                .to_fill = {},
                .to_empty = {},
                .set_list = {},
                .changes = {},
                .width = width,
                .height = height,
                .size = width * height,
                .start_x = start_x,
                .start_y = start_y,
                .mines = mines_amount,
                .allow_big_perturbs = (tries > 100 && width < 60 && height < 60),
                .random = random,
        };

        success = try_solve_minefield(ctx, random);
    } while (!success);

    return result;
}

std::size_t diff_abs(std::size_t a, std::size_t b) {
    if (a == b) {
        return 0;
    } else if (a > b) {
        return std::abs(static_cast<int>(a) - static_cast<int>(b));
    } else {
        return std::abs(static_cast<int>(b) - static_cast<int>(a));
    }
}

std::size_t checked_sub(std::size_t value) {
    if (value > 0) {
        return value - 1;
    } else {
        return 0;
    }
}

std::size_t checked_add(std::size_t value, std::size_t max) {
    if (value + 1 <= max) {
        return value + 1;
    } else {
        return max;
    }
}

std::size_t calc_safe_area(
        std::size_t width,
        std::size_t height,
        std::size_t start_x,
        std::size_t start_y
) {
    std::size_t max_x = width - 1;
    std::size_t max_y = height - 1;

    std::size_t left = checked_sub(start_x);
    std::size_t right = checked_add(start_x, max_x);
    std::size_t top = checked_sub(start_y);
    std::size_t bottom = checked_add(start_y, max_y);

    return (right - left + 1) * (bottom - top + 1);
}

std::basic_string<std::size_t> generate_random_minefield_with_safe_area(
        std::size_t width,
        std::size_t height,
        std::size_t mines_amount,
        std::size_t start_x,
        std::size_t start_y,
        std::mt19937 &random
) {
    const std::size_t size = width * height;
    std::basic_string<std::size_t> result = std::basic_string<std::size_t>(size, 0);

    std::size_t current_mines = 0;
    std::size_t remain_empty = size;
    std::size_t min_empty_squares = calc_safe_area(width, height, start_x, start_y);

    while (current_mines < mines_amount && remain_empty > min_empty_squares) {
        std::size_t mine_x = random_up_to(random, width);
        std::size_t mine_y = random_up_to(random, height);
        std::size_t dx = diff_abs(mine_x, start_x);
        std::size_t dy = diff_abs(mine_y, start_y);
        std::size_t index = mine_y * width + mine_x;

        if (result[index] == 0 && (dx > 1 || dy > 1)) {
            result[index] = 1;
            current_mines += 1;
            remain_empty -= 1;
        }
    }

    return result;
}

