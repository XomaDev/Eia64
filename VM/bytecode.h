//
// Created by kumaraswamy on 8/16/24.
//

#ifndef VM_BYTECODE_H
#define VM_BYTECODE_H

enum class bytecode {
    BOOL,
    INT,
    STRING,
    NIL,

    ADD,
    SUB,
    DIV,
    MUL,

    PRINT,
    HALT,
};

#endif //VM_BYTECODE_H
