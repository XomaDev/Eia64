//
// Created by kumaraswamy on 8/19/24.
//

#ifndef VM_MEMORY_FRAME_H
#define VM_MEMORY_FRAME_H


#include <cstdint>
#include <string>
#include <stack>
#include <vector>

class memory_frame {
    std::vector<uint64_t> memory_table;
    std::stack<uint64_t> stack_memory;
public:
    memory_frame(): memory_table(50) {
        // Meow
    }
    // for named memory
    void store(size_t index, uint64_t value);
    uint64_t load(size_t index);

    // stack operations
    void push(uint64_t value);

    uint64_t top();
    uint64_t pop();

    std::string* topString();
    std::string* popString();
};


#endif //VM_MEMORY_FRAME_H
