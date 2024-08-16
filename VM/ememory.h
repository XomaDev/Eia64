//
// Created by kumaraswamy on 8/16/24.
//

#ifndef VM_EMEMORY_H
#define VM_EMEMORY_H


#include <cstdint>
#include <stack>
#include "value.h"

class ememory {
    std::stack<Value> stack_memory;

public:
    void push(uint64_t value);
    void push(Value element);

    uint64_t pop_int();
    Value pop();
};


#endif //VM_EMEMORY_H
