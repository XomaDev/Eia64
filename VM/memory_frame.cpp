//
// Created by kumaraswamy on 8/19/24.
//

#include "memory_frame.h"

void memory_frame::store(size_t index, uint64_t value) {
    memory_table[index] = value;
}

uint64_t memory_frame::load(size_t index) {
    return memory_table[index];
}

void memory_frame::push(uint64_t value) {
    stack_memory.push(value);
}

uint64_t memory_frame::top() {
    return stack_memory.top();
}

uint64_t memory_frame::pop() {
    auto top = stack_memory.top();
    stack_memory.pop();
    return top;
}

std::string *memory_frame::topString() {
    return reinterpret_cast<std::string*>(top());
}

std::string *memory_frame::popString() {
    return reinterpret_cast<std::string*>(pop());
}