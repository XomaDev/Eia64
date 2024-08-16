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
    ADD_STR,
    SUB,
    DIV,
    MUL,

    PRINT,
    PRINT_STR,
    END_LINE,
    HALT,

    EQUAL,
    NOT_EQUAL,

    SCOPE_END,
};

#endif //VM_BYTECODE_H
