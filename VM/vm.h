//
// Created by kumaraswamy on 8/16/24.
//

#ifndef VM_VM_H
#define VM_VM_H

#include <memory>
#include "bytecode.h"
#include "ememory.h"

class vm {
    std::unique_ptr<uint8_t[]> bytes;
    long index = 0;
    ememory memory;

    uint8_t read();

    bytecode next();
    int32_t readInt32();
public:
    vm(std::unique_ptr<uint8_t[]> bytes): bytes(std::move(bytes))  {
        // Meow
    }

    void run();
};


#endif //VM_VM_H
