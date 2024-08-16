package space.themelon.eia64.compiler.bytecode

enum class Bytecode {
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
}