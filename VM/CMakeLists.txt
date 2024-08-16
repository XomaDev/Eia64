cmake_minimum_required(VERSION 3.29)
project(VM)

set(CMAKE_CXX_STANDARD 20)

add_executable(VM main.cpp
        vm.cpp
        vm.h
        bytecode.h
        ememory.cpp
        ememory.h
        assembler/assembler.cpp
        assembler/assembler.h
        assembler/bytecode_writer.cpp
        assembler/bytecode_writer.h)
