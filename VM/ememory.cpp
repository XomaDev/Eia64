//
// Created by kumaraswamy on 8/16/24.
//

#include <iostream>
#include "ememory.h"

void ememory::store(size_t index, uint64_t value) {
    memory_table[index] = value;
}

uint64_t ememory::load(size_t index) {
    return memory_table[index];
}

void ememory::push(uint64_t value) {
    stack_memory.push(value);
}

uint64_t ememory::top() {
    return stack_memory.top();
}

uint64_t ememory::pop() {
    auto top = stack_memory.top();
    stack_memory.pop();
    std::cout << "Pop(): " << std::to_string(top) << std::endl << std::flush;
    return top;
}

std::string *ememory::topString() {
    return reinterpret_cast<std::string*>(top());
}

std::string *ememory::popString() {
    return reinterpret_cast<std::string*>(pop());
}
