package space.themelon.eia64.compiler.bytecode

enum class Bytecode {
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
    HALT,
}