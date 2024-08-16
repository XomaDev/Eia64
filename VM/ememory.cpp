//
// Created by kumaraswamy on 8/16/24.
//

#include "ememory.h"

void ememory::push_stack(uint64_t element) {
    stack_memory.push(element);
}

uint64_t ememory::pop_stack() {
    auto top = stack_memory.top();
    stack_memory.pop();
    return top;
}
