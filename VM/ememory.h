//
// Created by kumaraswamy on 8/16/24.
//

#ifndef VM_EMEMORY_H
#define VM_EMEMORY_H


#include <cstdint>
#include <stack>
#include <string>
#include <vector>

class ememory {
    std::stack<uint64_t> stack_memory;
    std::vector<uint64_t> memory_table;

public:
    ememory(): memory_table(100) {
        // Meow
    }

    void store(size_t index, uint64_t value);
    uint64_t load(size_t index);

    void push(uint64_t value);

    uint64_t top();
    uint64_t pop();

    std::string* topString();
    std::string* popString();
};


#endif //VM_EMEMORY_H
