#ifndef ANTIMINE_ANDROID_RANDOM_GENERATOR_H
#define ANTIMINE_ANDROID_RANDOM_GENERATOR_H

#include <random>

std::uint64_t random_bits(std::mt19937& random);

std::uint64_t random_up_to(std::mt19937& random, std::size_t limit);

std::size_t random_index_of(
        std::mt19937& random,
        const std::basic_string<std::size_t>& origin,
        std::size_t value,
        const std::basic_string<std::size_t>& except
);

#endif //ANTIMINE_ANDROID_RANDOM_GENERATOR_H
