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

    // Unary
    NEG,
    NOT,

    // SYSTEM
    READ,
    READ_LN,
    PRINT,
    PRINT_STR,
    END_LINE,
    HALT,
    SLEEP,
    STR_LEN,
    TO_STR, // Int to String
    TO_CH, // Int to Single Letter String
    POP,
    POP_STR, // remove String from Stack
    STORE,
    LOAD,
    CHAR_AT, // element at

    SCOPE,
    DECIDE,
    ENTER_FRAME,
    EXIT_FRAME,

    // Logical
    INT_CMP, // compares last two Ints
    STR_CMP, // compares last two Strings

    GREATER_THAN,
    LESSER_THAN,
    GREATER_EQ,
    LESSER_EQ,
    AND,
    OR,

    GO,
    VISIT,

    VISIT_EQUAL,
    VISIT_UNEQUAL,

    GO_EQUAL,
    GO_UNEQUAL,

    SCOPE_END,
};

#endif //VM_BYTECODE_H
