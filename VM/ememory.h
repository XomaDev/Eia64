//
// Created by kumaraswamy on 8/16/24.
//

#ifndef VM_EMEMORY_H
#define VM_EMEMORY_H


#include <cstdint>
#include <stack>
#include <string>

class ememory {
    std::stack<uint64_t> stack_memory;

public:
    void push(uint64_t value);

    uint64_t pop();
    std::string* popString();
};


#endif //VM_EMEMORY_H
