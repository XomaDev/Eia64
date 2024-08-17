//
// Created by kumaraswamy on 8/16/24.
//

#ifndef VM_VM_H
#define VM_VM_H

#include <memory>
#include <unordered_map>
#include "bytecode.h"
#include "ememory.h"

class vm {
    std::unique_ptr<uint8_t[]> bytes;
    std::unordered_map<std::string, long> scopes;

    unsigned long index = 0;
    unsigned long size;
    ememory memory;

    std::string* readString();
    uint8_t read();

    bytecode next();
    int32_t readInt32();

    bool hasMore();
    bool running = true;
public:
    vm(std::unique_ptr<uint8_t[]> bytes, long size): bytes(std::move(bytes)), size(size)  {
        // Meow
    }
    ~vm();

    void run();
    bool run_scope();
};


#endif //VM_VM_H
