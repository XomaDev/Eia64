//
// Created by kumaraswamy on 8/16/24.
//

#include <iostream>
#include <chrono>
#include <thread>
#include "vm.h"

void vm::run() {
    // TODO:
    //  For Future, whenever we allocate Strings
    //  We gotta make note of their addresses and clear them
    //  All at the end
    //  Or just make the user delete it? by like .txt section?
    while (hasMore() && running) {
        auto scope_name = readString();
        auto copy_name = std::string(*scope_name);
        delete scope_name;
        scopes[copy_name] = index;
        if (copy_name == "main") {
            if (run_scope()) break;
        } else {
            // skip the scope
            index += readInt32();
        }
    }
}

bool vm::run_scope() {
    for (; running;) {
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
            case bytecode::NOT: {
                memory.push(!memory.pop());
                break;
            }

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
                std::cout << std::flush;
                break;
            case bytecode::PRINT_STR: {
                auto *string = memory.popString();
                std::cout << *string << std::flush;
                break;
            }
            case bytecode::END_LINE:
                std::cout << std::endl << std::flush;
                break;
            case bytecode::HALT: {
                running = false;
                return true;
            }
            case bytecode::SLEEP:
                std::this_thread::sleep_for(std::chrono::milliseconds(readInt32()));
                break;

            case bytecode::SCOPE_END:
                return false;
            case bytecode::TO_STR: {
                auto aString = new std::string(std::to_string(memory.pop()));
                memory.push(reinterpret_cast<uint64_t>(aString));
                break;
            }
            case bytecode::TO_CH: {
                auto single_letter = new std::string(1, static_cast<char>(memory.top()));
                memory.push(reinterpret_cast<uint64_t>(single_letter));
                break;
            }
            case bytecode::STR_LEN:
                memory.push(memory.topString()->size());
                break;
            case bytecode::POP:
                memory.pop();
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
            case bytecode::CHAR_AT: {
                auto at_index = memory.pop();
                memory.push((*memory.topString())[at_index]);
                break;
            }
            case bytecode::SCOPE: {
                auto scopeName = *readString();
                //std::cout << "Scope name " << scopeName << std::endl << std::flush;
                memory.push(scopes[scopeName]);
                break;
            }
            case bytecode::DECIDE: {
                // OK_SCOPE
                // NO_SCOPE
                // INT CMP
                // BOOL COME_BACK
                bool comeBack = memory.pop();
                // what it does?
                // pop()s out latest element, if true, executes [stack - 2] else [stack - 1]
                uint64_t scopeIndex;
                if (memory.pop()) {
                    //std::cout << "Truth decide" << std::endl << std::flush;
                    memory.pop();
                    scopeIndex = memory.pop();
                } else {
                    scopeIndex = memory.pop();
                }

                if (comeBack) {
                    auto currentIndex = index;
                    index = scopeIndex;
                    run_scope();
                    index = currentIndex;
                } else {
                    index = scopeIndex;
                    return run_scope();
                }
            }

            case bytecode::INT_CMP:
                memory.push(memory.pop() == memory.pop() ? 1 : 0);
                break;
            case bytecode::STR_CMP:
                memory.push((*memory.popString() == *memory.popString()) ? 1 : 0);
                break;

            case bytecode::GO:
                index = memory.pop();
                return run_scope();

            case bytecode::VISIT: {
                auto current_index = index;
                index = memory.pop();
                run_scope();
                index = current_index;
                break;
            }
            case bytecode::VISIT_EQUAL: {
                auto scopeIndex = memory.pop();

                // visits and comes back
                if (memory.pop() != 1) break;
                auto current_index = index;
                index = scopeIndex;
                run_scope();
                index = current_index;
            }
            case bytecode::VISIT_UNEQUAL: {
                auto scopeIndex = memory.pop();

                // visits and comes back
                if (memory.pop() != 0) break;
                auto current_index = index;
                index = scopeIndex;
                run_scope();
                index = current_index;
            }
            case bytecode::GO_EQUAL: {
                auto scopeIndex = memory.pop();

                // goes... but never comes bacc
                if (memory.pop() != 1) break;
                index = scopeIndex;
                return run_scope();
            }
            case bytecode::GO_UNEQUAL: {
                auto scopeIndex = memory.pop();

                // goes... but never comes bacc
                auto cmp = memory.pop();
                if (cmp != 0) break;
                index = scopeIndex;
                return run_scope();
            }

            default:
                throw std::runtime_error("Unknown bytecode " +
                                         std::to_string(static_cast<int>(op_code)) + ", at index " +
                                         std::to_string(index));
            case bytecode::NIL:
                break;
        }
    }
    throw std::runtime_error("Reached Unexpected End Of Loop");
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
           (read() & 0xff) << 16 |
           (read() & 0xff) << 8 |
           (read() & 0xff);
}

bool vm::hasMore() {
    return index < size;
}


vm::~vm() {
    //for (const auto &scope: scopes) {
    //delete &scope.first;
    //}
}