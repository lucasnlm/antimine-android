#pragma  once

#include <random>

std::uint8_t random_bits(std::mt19937& random);

std::uint64_t random_up_to(std::mt19937& random, std::size_t limit);
