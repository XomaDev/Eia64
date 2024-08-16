//
// Created by kumaraswamy on 8/16/24.
//

#ifndef VM_VALUE_H
#define VM_VALUE_H

#include <cstdint>

enum ValueType {
    VAL_BOOL,
    VAL_INT
};

struct Value {
    ValueType type;
    union {
        uint64_t number;
        bool boolean;
    } as;
};


#endif //VM_VALUE_H
