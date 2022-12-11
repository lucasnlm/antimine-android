#include "random_generator.h"

std::uint8_t random_bits(std::mt19937& random) {
    static std::uint64_t last = 0L;

    if (last == 0L) {
        std::uniform_int_distribution<std::mt19937::result_type> dist;
        last = dist(random);
    }

    std::uint8_t result = last & 0xFF;
    last = last << 8;
    return result;
}

std::uint64_t random_up_to(std::mt19937& random, std::size_t limit) {
    std::uniform_int_distribution<std::mt19937::result_type> dist(0,limit - 1);
    return dist(random);
}
