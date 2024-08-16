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
            case bytecode::HALT:
                break;
        }
    }
}

bytecode vm::next() {
    return static_cast<bytecode>(bytes[index++]);
}