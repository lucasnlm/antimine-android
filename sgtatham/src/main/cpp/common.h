#pragma once

#include <random>

#define snew(type) \
    ( (type *) smalloc (sizeof (type)) )

struct mine_context {
    std::basic_string<std::size_t> grid;
    const std::size_t width;
    const std::size_t height;
    const std::size_t size;
    const std::size_t start_x;
    const std::size_t start_y;
    const std::size_t mines;
    const bool allow_big_perturbs;
    std::mt19937 &random;
};
