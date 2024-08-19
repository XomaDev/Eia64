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
    auto start = std::chrono::high_resolution_clock::now();

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
    auto stop = std::chrono::high_resolution_clock::now();
    auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(stop - start);
    std::cout << "Time taken: " << duration.count() << " milliseconds" << std::endl;
}

bool vm::run_scope() {
    for (; running;) {
        auto op_code = next();
        switch (op_code) {
            // Binary operations
            case bytecode::INT:
                frame->push(readInt32());
                break;
            case bytecode::BOOL:
                frame->push(read() == 1);
                break;
            case bytecode::STRING:
                // store the string's memory address
                frame->push(reinterpret_cast<uint64_t>(readString()));
                break;
            case bytecode::ADD:
                frame->push(frame->pop() + frame->pop());
                break;
            case bytecode::ADD_STR: {
                auto *right = frame->popString();
                auto *left = frame->popString();

                //std::cout << "Right:" << *right << ", left:" << *left << std::endl << std::flush;
                auto newString = new std::string(*left + *right);
                //std::cout << "New String " << *newString << std::endl << std::flush;
                frame->push(reinterpret_cast<uint64_t>(newString));
                break;
            }
            case bytecode::SUB: {
                auto right = frame->pop();
                auto left = frame->pop();
                frame->push(left - right);
                break;
            }
            case bytecode::MUL:
                frame->push(frame->pop() * frame->pop());
                break;
            case bytecode::DIV: {
                auto right = frame->pop();
                auto left = frame->pop();
                frame->push(left / right);
                break;
            }

                // Unary operations
            case bytecode::NEG:
                frame->push(-frame->pop());
                break;
            case bytecode::NOT: {
                frame->push(!frame->pop());
                break;
            }

                // System calls
            case bytecode::READ: {
                uint64_t number;
                std::cin >> number;
                frame->push(number);
                break;
            }
            case bytecode::READ_LN: {
                std::string line;
                getline(std::cin, line);
                frame->push(reinterpret_cast<uint64_t>(new std::string(line)));
                break;
            }
            case bytecode::PRINT:
                printf("%d", static_cast<int32_t>(frame->pop()));
                std::cout << std::flush;
                break;
            case bytecode::PRINT_STR: {
                auto *string = frame->popString();
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
                std::this_thread::sleep_for(std::chrono::milliseconds(frame->pop()));
                break;

            case bytecode::SCOPE_END:
                return false;
            case bytecode::TO_STR: {
                auto aString = new std::string(std::to_string(frame->pop()));
                frame->push(reinterpret_cast<uint64_t>(aString));
                break;
            }
            case bytecode::TO_CH: {
                auto single_letter = new std::string(1, static_cast<char>(frame->pop()));
                //std::cout << "To Ch " << *single_letter << std::endl << std::flush;
                frame->push(reinterpret_cast<uint64_t>(single_letter));
                break;
            }
            case bytecode::STR_LEN: {
                auto strLen = (readInt32() ? frame->popString() : frame->topString())->size();
                frame->push(strLen);
                break;
            }
            case bytecode::POP:
                frame->pop();
                break;
            case bytecode::POP_STR:
                frame->popString();
                break;
            case bytecode::STORE:
                frame->store(readInt32(), frame->top());
                break;
            case bytecode::LOAD:
                frame->push(frame->load(readInt32()));
                break;
            case bytecode::CHAR_AT: {
                auto at_index = frame->pop();
                auto string = *frame->topString();
                //  std::cout << "String: " << string << " index " << std::to_string(at_index) << std::endl << std::flush;
                frame->push((string)[at_index]);
                break;
            }
            case bytecode::SCOPE: {
                auto scopeName = *readString();
                frame->push(scopes[scopeName]);
                break;
            }
            case bytecode::DECIDE: {
                // OK_SCOPE
                // NO_SCOPE
                // INT CMP
                // BOOL COME_BACK
                bool comeBack = frame->pop();
                // what it does?
                // pop()s out latest element, if true, executes [stack - 2] else [stack - 1]
                uint64_t scopeIndex;
                if (frame->pop()) {
                    //std::cout << "Truth decide" << std::endl << std::flush;
                    frame->pop();
                    scopeIndex = frame->pop();
                } else {
                    scopeIndex = frame->pop();
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
                break;
            }
            case bytecode::ENTER_FRAME: {
                // number of args to copy from current frame to newer frame
                auto argsCopy = readInt32();
                auto stackElements = popElements(argsCopy);
                frame = memory.enterFrame();
                pushElements(argsCopy, stackElements);
                break;
            }
            case bytecode::EXIT_FRAME: {
                // number of args to copy from current frame to older frame
                auto argsCopy = readInt32();
                auto stackElements = popElements(argsCopy);
                frame = memory.exitFrame();
                pushElements(argsCopy, stackElements);
                break;
            }

            case bytecode::INT_CMP:
                frame->push(frame->pop() == frame->pop() ? 1 : 0);
                break;
            case bytecode::STR_CMP:
                frame->push((*frame->popString() == *frame->popString()) ? 1 : 0);
                break;

            case bytecode::GREATER_THAN: {
                auto right = frame->pop();
                auto left = frame->pop();
                frame->push(left > right);
                break;
            }

            case bytecode::LESSER_THAN: {
                auto right = frame->pop();
                auto left = frame->pop();
                frame->push(left < right);
                break;
            }

            case bytecode::GREATER_EQ: {
                auto right = frame->pop();
                auto left = frame->pop();
                frame->push(left >= right);
                break;
            }

            case bytecode::LESSER_EQ: {
                auto right = frame->pop();
                auto left = frame->pop();
                frame->push(left <= right);
                break;
            }

            case bytecode::AND:
                frame->push(frame->pop() && frame->pop());
                break;

            case bytecode::OR:
                frame->push(frame->pop() || frame->pop());
                break;

            case bytecode::GO:
                index = frame->pop();
                return run_scope();

            case bytecode::VISIT: {
                auto current_index = index;
                index = frame->pop();
                run_scope();
                index = current_index;
                break;
            }
            case bytecode::VISIT_EQUAL: {
                auto scopeIndex = frame->pop();

                // visits and comes back
                if (frame->pop() != 1) break;
                auto current_index = index;
                index = scopeIndex;
                run_scope();
                index = current_index;
                break;
            }
            case bytecode::VISIT_UNEQUAL: {
                auto scopeIndex = frame->pop();

                // visits and comes back
                if (frame->pop() != 0) break;
                auto current_index = index;
                index = scopeIndex;
                run_scope();
                index = current_index;
                break;
            }
            case bytecode::GO_EQUAL: {
                auto scopeIndex = frame->pop();

                // goes... but never comes bacc
                if (frame->pop() != 1) break;
                index = scopeIndex;
                return run_scope();
            }
            case bytecode::GO_UNEQUAL: {
                auto scopeIndex = frame->pop();

                // goes... but never comes bacc
                auto cmp = frame->pop();
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

void vm::pushElements(int32_t argsCopy, const uint64_t *stackElements) {
    for (int i = 0; i < argsCopy; ++i) {
        frame->push(stackElements[i]);
    }
    delete[] stackElements;
}

uint64_t *vm::popElements(int32_t argsCopy) {
    auto stackElements = new uint64_t[argsCopy];
    for (int i = 0; i < argsCopy; ++i) {
        stackElements[i] = frame->pop();
    }
    return stackElements;
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
    memory.close();
    //for (const auto &scope: scopes) {
    //delete &scope.first;
    //}
}