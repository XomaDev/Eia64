//
// Created by kumaraswamy on 8/16/24.
//

#ifndef VM_EMEMORY_H
#define VM_EMEMORY_H


#include <cstdint>
#include <stack>
#include <string>
#include "value.h"

class ememory {
    std::stack<uint64_t> stack_memory;

public:
    void push(uint64_t value);
    uint64_t pop();
};


#endif //VM_EMEMORY_H
