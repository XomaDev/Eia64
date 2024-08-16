//
// Created by kumaraswamy on 8/16/24.
//

#ifndef VM_BYTECODE_H
#define VM_BYTECODE_H

enum class bytecode {
    // Types
    BOOL,
    INT,
    STRING,
    NIL,

    // Binary
    ADD,
    ADD_STR,
    SUB,
    DIV,
    MUL,

    // SYSTEM
    PRINT,
    PRINT_STR,
    END_LINE,
    HALT,

    // Conversion
    TO_STR, // Int to String

    // Logical
    INT_CMP, // compares last two ints
    STR_CMP, // compares last two strings

    GO,

    GO_EQUAL,
    GO_UNEQUAL,

    SCOPE_END,
};

#endif //VM_BYTECODE_H
