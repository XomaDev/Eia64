//
// Created by kumaraswamy on 8/16/24.
//

#include <iostream>
#include "vm.h"

void vm::run() {
    while (hasMore() && running) {
        auto scope_name = readString();
        auto copy_name = std::string(*scope_name);
        delete scope_name;
        scopes[copy_name] = index;
        if (copy_name == "main") {
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
    for (;running;) {
        auto op_code = next();
        switch (op_code) {
            // Binary operations
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

            // Unary operations
            case bytecode::NEG:
                memory.push(-memory.pop());
                break;
            case bytecode::NOT:
                memory.push(!memory.pop());
                break;

            // System calls
            case bytecode::READ: {
                uint64_t number;
                std::cin >> number;
                memory.push(number);
                break;
            }
            case bytecode::READ_LN: {
                std::string line;
                getline(std::cin, line);
                memory.push(reinterpret_cast<uint64_t>(new std::string(line)));
                break;
            }
            case bytecode::PRINT:
                printf("%d", static_cast<int32_t>(memory.pop()));
                break;
            case bytecode::PRINT_STR: {
                auto *string = memory.popString();
                std::cout << *string;
                delete string;
                break;
            }
            case bytecode::END_LINE:
                std::cout << std::endl;
                break;
            case bytecode::HALT: {
                running = false;
                return true;
            }
            case bytecode::SCOPE_END: return false;
            case bytecode::TO_STR: {
                auto aString = new std::string(std::to_string(memory.pop()));
                memory.push(reinterpret_cast<uint64_t>(aString));
                break;
            }
            case bytecode::STR_LEN:
                memory.push(memory.topString()->size());
                break;
            case bytecode::POP_STR:
                delete memory.popString();
                break;
            case bytecode::STORE:
                memory.store(readInt32(), memory.top());
                break;
            case bytecode::LOAD:
                memory.push(memory.load(readInt32()));
                break;

            case bytecode::INT_CMP:
                memory.push(memory.pop() == memory.pop() ? 1 : 0);
                break;
            case bytecode::STR_CMP:
                memory.push((*memory.popString() == *memory.popString()) ? 1 : 0);
                break;

            case bytecode::GO:
                return go_scope(readString());

            case bytecode::GO_EQUAL: {
                auto scope_name = readString();
                if (memory.top() != 1) break;
                return go_scope(scope_name);
            }

            case bytecode::GO_UNEQUAL: {
                auto scope_name = readString();
                if (memory.top() != 0) break;
                return go_scope(scope_name);
            }

            default:
                throw std::runtime_error("Unknown bytecode " + std::to_string(static_cast<int>(op_code)));
        }
    }
    throw std::runtime_error("Reached Unexpected End Of Loop");
}

bool vm::go_scope(std::string* scope_name) {
    index = scopes[*scope_name];
    delete scope_name;
    return run_scope();
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