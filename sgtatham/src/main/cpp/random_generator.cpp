#include "random_generator.h"

std::uint64_t random_bits(std::mt19937& random) {
    std::uniform_int_distribution<std::mt19937::result_type> dist;
    return dist(random);
}

std::uint64_t random_up_to(std::mt19937& random, std::size_t limit) {
    std::uniform_int_distribution<std::mt19937::result_type> dist(0,limit - 1);
    return dist(random);
}

std::size_t random_index_of(
        std::mt19937& random,
        const std::basic_string<std::size_t>& origin,
        std::size_t value,
        const std::basic_string<std::size_t>& except
) {
    std::size_t result = std::basic_string<std::size_t>::npos;

    std::basic_string<std::size_t> exceptions;
    exceptions.reserve(except.size() + 16);
    exceptions.append(except);

    if (!origin.empty()) {
        do {
            auto index = random_up_to(random, origin.size());

            if (origin[index] == value) {
                auto valid_index = exceptions.find(index);
                if (valid_index == std::basic_string<std::size_t>::npos) {
                    result = index;
                    break;
                }
            }
        } while (exceptions.size() < origin.size());
    }

    return result;
}