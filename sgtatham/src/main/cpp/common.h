#pragma once

#include <random>

#define snew(type) \
    ( (type *) smalloc (sizeof (type)) )

struct square {
    std::size_t id;
    std::size_t x;
    std::size_t y;
    int type;
    std::uint8_t random;
};

struct perturbation {
    std::size_t x;
    std::size_t y;
    int delta; // +1 == become a mine; -1 == cleared
};

struct mine_context {
    std::basic_string<std::size_t>& grid;
    std::vector<square> square_list;
    std::vector<std::size_t> to_fill;
    std::vector<std::size_t> to_empty;
    std::vector<std::size_t> set_list;
    std::vector<perturbation> changes;
    const std::size_t width;
    const std::size_t height;
    const std::size_t size;
    const std::size_t start_x;
    const std::size_t start_y;
    const std::size_t mines;
    const bool allow_big_perturbs;
    std::mt19937 &random;
};
