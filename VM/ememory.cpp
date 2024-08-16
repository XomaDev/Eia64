//
// Created by kumaraswamy on 8/16/24.
//

#include <iostream>
#include "ememory.h"

void ememory::push(uint64_t value) {
    stack_memory.push(value);
}

uint64_t ememory::pop() {
    auto top = stack_memory.top();
    stack_memory.pop();
    return top;
}

std::string *ememory::popString() {
    return reinterpret_cast<std::string*>(pop());
}
