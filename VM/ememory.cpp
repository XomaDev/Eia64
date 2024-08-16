//
// Created by kumaraswamy on 8/16/24.
//

#include "ememory.h"

void ememory::push(uint64_t anInt) {
    auto value = Value(VAL_INT);
    value.as.number = anInt;
    stack_memory.push(value);
}

void ememory::push(bool boolean) {
    auto value = Value(VAL_BOOL);
}

void ememory::push(Value element) {
    stack_memory.push(element);
}

Value ememory::pop() {
    auto top = stack_memory.top();
    stack_memory.pop();
    return top;
}

uint64_t ememory::pop_int() {
    return pop().as.number;
}
