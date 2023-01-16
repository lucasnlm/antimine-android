cmake_minimum_required(VERSION 3.10.2)

project("sgtatham")

add_library(# Sets the name of the library.
            sgtatham

            # Sets the library as a shared library.
            SHARED

            tree234.cpp
            random_generator.cpp
            minefield_creator.cpp
            sgtatham.cpp)

target_link_libraries(# Specifies the target library.
                      sgtatham)

set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -s -o3")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -s -o3")