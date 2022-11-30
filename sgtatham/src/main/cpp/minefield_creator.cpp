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
    std::basic_string<std::size_t> result(width * height, 0);

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
                .width = width,
                .height = height,
                .size = width * height,
                .start_x = start_x,
                .start_y = start_y,
                .mines = mines_amount,
                .allow_big_perturbs = (tries > 100),
                .random = random,
        };

        success = try_solve_minefield(ctx, random);
    } while (!success);

    return result;
}

std::basic_string<std::size_t> generate_random_minefield_with_safe_area(
        std::size_t width,
        std::size_t height,
        std::size_t mines_amount,
        std::size_t start_x,
        std::size_t start_y,
        std::mt19937 &random
) {
    std::basic_string<std::size_t> result =
            generate_random_minefield(width, height, mines_amount, random);

    const std::size_t safe_places_array[] = {
            (start_y + 0) * width + (start_x + 0),
            (start_y + 0) * width + (start_x + 0),
            (start_y + 0) * width + (start_x + 0),
            (start_y + 0) * width + (start_x + 1),
            (start_y + 0) * width + (start_x - 1),
            (start_y + 1) * width + (start_x + 0),
            (start_y - 1) * width + (start_x + 0),
            (start_y + 1) * width + (start_x + 1),
            (start_y + 1) * width + (start_x - 1),
            (start_y - 1) * width + (start_x + 1),
            (start_y - 1) * width + (start_x - 1),
    };
    std::basic_string<std::size_t> safe_places(safe_places_array);

    for (std::size_t safe_place: safe_places_array) {
        if (result[safe_place] != 0) {
            auto random_index = random_index_of(random, result, 0, safe_places);
            if (random_index != std::basic_string<std::size_t>::npos) {
                result[random_index] = result[safe_place];
            }
        }
        result[safe_place] = 0;
    }

    return result;
}

std::basic_string<std::size_t> generate_random_minefield(
        std::size_t width,
        std::size_t height,
        std::size_t mines_amount,
        std::mt19937 &random
) {
    const std::size_t size = width * height;
    std::basic_string<std::size_t> result = std::basic_string<std::size_t>(size - mines_amount, 0) +
                                            std::basic_string<std::size_t>(mines_amount, 1);
    std::shuffle(result.begin(), result.end(), random);
    return result;
}
