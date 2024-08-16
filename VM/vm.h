//
// Created by kumaraswamy on 8/16/24.
//

#ifndef VM_VM_H
#define VM_VM_H

#include <memory>
#include "bytecode.h"

class vm {
    std::unique_ptr<uint8_t[]> bytes;
    long index = 0;

    bytecode next();
public:
    vm(std::unique_ptr<uint8_t[]> bytes): bytes(std::move(bytes))  {
        // Meow
    }

    void run();
};


#endif //VM_VM_H
