//
// Created by kumaraswamy on 8/16/24.
//

#ifndef VM_EMEMORY_H
#define VM_EMEMORY_H


#include <cstdint>
#include <stack>
#include <string>
#include <vector>
#include "memory_frame.h"

class ememory {
    std::stack<memory_frame *> frames;
public:

    memory_frame* enterFrame();
    memory_frame* exitFrame();
    void close();
};


#endif //VM_EMEMORY_H
