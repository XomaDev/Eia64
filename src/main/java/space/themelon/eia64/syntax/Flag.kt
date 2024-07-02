package space.themelon.eia64.syntax

enum class Flag {
    ASSIGNMENT_TYPE,
    LOGICAL_OR, LOGICAL_AND,
    BITWISE_OR, BITWISE_AND,

    EQUALITY, RELATIONAL, BINARY, BINARY_L2, BINARY_L3,
    OPERATOR, PRESERVE_ORDER,
    UNARY, POSSIBLE_RIGHT_UNARY,

    CLASS,
    E_BOOL,

    VALUE,

    V_KEYWORD,

    LOOP,
    NATIVE_CALL,
    INTERRUPTION,
    NONE,
}