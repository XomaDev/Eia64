//
// Created by kumaraswamy on 8/16/24.
//

#ifndef VM_EMEMORY_H
#define VM_EMEMORY_H


#include <cstdint>
#include <stack>

class ememory {
    std::stack<uint64_t> stack_memory;

public:
    void push_stack(uint64_t element);
    uint64_t pop_stack();
};


#endif //VM_EMEMORY_H
