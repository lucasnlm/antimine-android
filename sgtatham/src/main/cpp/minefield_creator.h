#pragma once

#include <string>
#include <random>

std::string new_mine_layout(
        int slice_width,
        std::size_t width,
        std::size_t height,
        std::size_t mines_amount,
        std::size_t x_position,
        std::size_t y_position,
        std::mt19937 &random
);

std::string minefield_to_string(const std::basic_string<std::size_t> &grid);

std::basic_string<std::size_t> generate_safe_minefield_sliced(
        std::size_t slice_width,
        std::size_t width,
        std::size_t height,
        std::size_t mines_amount,
        std::size_t start_x,
        std::size_t start_y,
        std::mt19937 &random
);

std::basic_string<std::size_t> generate_safe_minefield(
        std::size_t width,
        std::size_t height,
        std::size_t mines_amount,
        std::size_t start_x,
        std::size_t start_y,
        bool safe_border,
        std::mt19937 &random
);

std::basic_string<std::size_t> generate_random_minefield_with_safe_area(
        std::size_t width,
        std::size_t height,
        std::size_t mines_amount,
        std::size_t start_x,
        std::size_t start_y,
        bool safe_border,
        std::mt19937 &random
);
