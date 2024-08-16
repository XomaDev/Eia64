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
                memory.push(readInt32());
                break;
            case bytecode::BOOL:
                memory.push(read() == 1);
                break;
            case bytecode::STRING:
                // store the string's memory address
                memory.push(reinterpret_cast<uint64_t>(readString()));
                break;
            case bytecode::ADD:
                memory.push(memory.pop() + memory.pop());
                break;
            case bytecode::ADD_STR: {
                auto* left = reinterpret_cast<std::string*>(memory.pop());
                auto* right = reinterpret_cast<std::string*>(memory.pop());
                memory.push(reinterpret_cast<uint64_t>(new std::string(*left + *right)));
                delete left;
                delete right;
                break;
            }
            case bytecode::SUB:
                memory.push(memory.pop() - memory.pop());
                break;
            case bytecode::MUL:
                memory.push(memory.pop() * memory.pop());
                break;
            case bytecode::DIV:
                memory.push(memory.pop() / memory.pop());
                break;
            case bytecode::PRINT:
                printf("%d\n", static_cast<int32_t>(memory.pop()));
                break;
            case bytecode::PRINT_STR: {
                auto* string = reinterpret_cast<std::string*>(memory.pop());
                printf("%c\n", *string->c_str());
                delete string;
                break;
            }
            case bytecode::HALT:
                return;
        }
    }
}

std::string* vm::readString() {
    uint8_t str_len = read();
    auto* name = new std::string(reinterpret_cast<const char *>(&bytes[index]), str_len);
    index += str_len;
    return name;
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