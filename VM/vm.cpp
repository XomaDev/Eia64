//
// Created by kumaraswamy on 8/16/24.
//

#include <iostream>
#include "vm.h"

void vm::run() {
    while (hasMore()) {
        auto scope_name = readString();
        scopes[*scope_name] = index;
        if (*scope_name == "main") {
            if (run_scope()) break;
        } else {
            for (;;) {
                auto code = read();
                if (code == static_cast<int>(bytecode::SCOPE_END)) break;
            }
        }
    }
}

bool vm::run_scope() {
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
                auto *right = memory.popString();
                auto *left = memory.popString();
                memory.push(reinterpret_cast<uint64_t>(new std::string(*left + *right)));
                delete left;
                delete right;
                break;
            }
            case bytecode::SUB: {
                auto right = memory.pop();
                auto left = memory.pop();
                memory.push(left - right);
                break;
            }
            case bytecode::MUL:
                memory.push(memory.pop() * memory.pop());
                break;
            case bytecode::DIV: {
                auto right = memory.pop();
                auto left = memory.pop();
                memory.push(left / right);
                break;
            }
            case bytecode::PRINT:
                printf("%d", static_cast<int32_t>(memory.pop()));
                break;
            case bytecode::PRINT_STR: {
                auto *string = reinterpret_cast<std::string *>(memory.pop());
                std::cout << *string;
                delete string;
                break;
            }
            case bytecode::END_LINE:
                std::cout << std::endl;
                break;
            case bytecode::HALT: return true;
            case bytecode::SCOPE_END: return false;

            case bytecode::TO_STR: {
                auto aString = new std::string(std::to_string(memory.pop()));
                memory.push(reinterpret_cast<uint64_t>(aString));
                break;
            }

            case bytecode::INT_CMP:
                memory.push(memory.pop() == memory.pop() ? 1 : 0);
                break;
            case bytecode::STR_CMP:
                memory.push((*memory.popString() == *memory.popString()) ? 1 : 0);
                break;

            case bytecode::GO: {
                go_scope();
                break;
            }

            case bytecode::GO_EQUAL: {
                if (memory.pop() != 1) break;
                go_scope();
                break;
            }

            case bytecode::GO_UNEQUAL: {
                if (memory.pop() != 0) break;
                go_scope();
                break;
            }

            default:
                throw std::runtime_error("Unknown bytecode " + std::to_string(static_cast<int>(op_code)));
        }
    }
}

void vm::go_scope() {
    auto scope_name = readString();
    long current_index = index;
    index = scopes[*scope_name];
    delete scope_name;
    run_scope();
    // just run and come back
    index = current_index;
}

std::string *vm::readString() {
    uint8_t str_len = read();
    auto *name = new std::string(reinterpret_cast<const char *>(&bytes[index]), str_len);
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
    return (read() & 0xff << 24) |
           (read() & 0xff) |
           (read() & 0xff) |
           (read() & 0xff);;
}

bool vm::hasMore() {
    return index < size;
}


vm::~vm() {
    //for (const auto &scope: scopes) {
        //delete &scope.first;
    //}
}