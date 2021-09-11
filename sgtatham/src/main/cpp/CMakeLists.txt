cmake_minimum_required(VERSION 3.10.2)

project("sgtatham")

add_library(# Sets the name of the library.
            sgtatham

            # Sets the library as a shared library.
            SHARED

            tree234.cpp
            sgtatham.cpp)

find_library(# Sets the name of the path variable.
             log-lib

             # Specifies the name of the NDK library that
             # you want CMake to locate.
             log )

target_link_libraries(# Specifies the target library.
                      sgtatham

                      # Links the target library to the log library
                      # included in the NDK.
                      ${log-lib} )