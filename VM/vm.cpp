//
// Created by kumaraswamy on 8/16/24.
//

#include <iostream>
#include "vm.h"

void vm::run() {
    std::cout << "meow" << std::endl;
    for (;;) {
        auto op_code = next();
        switch (op_code) {
            case bytecode::INT:
                memory.push_stack(readInt32());
                break;
            case bytecode::ADD: {
                memory.push_stack(memory.pop_stack() + memory.pop_stack());
                break;
            }
            case bytecode::SUB: {
                memory.push_stack(memory.pop_stack() - memory.pop_stack());
                break;
            }
            case bytecode::MUL: {
                memory.push_stack(memory.pop_stack() * memory.pop_stack());
                break;
            }
            case bytecode::DIV: {
                memory.push_stack(memory.pop_stack() / memory.pop_stack());
                break;
            }
            case bytecode::PRINT:
                printf("%d\n", static_cast<int32_t>(memory.pop_stack()));
                break;
            case bytecode::HALT:
                return;
        }
    }
}

uint8_t vm::read() {
    return bytes[index++];
}

bytecode vm::next() {
    return static_cast<bytecode>(bytes[index++]);
}

int32_t vm::readInt32() {
    return read() & 0xff |
           (read() & 0xff) << 8 |
           (read() & 0xff) << 16 |
           (read() & 0xff) << 24;;
}