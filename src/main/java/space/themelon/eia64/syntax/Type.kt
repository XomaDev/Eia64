package space.themelon.eia64.syntax

import java.util.*
import kotlin.collections.HashMap

enum class Type {

    LOGICAL_AND, LOGICAL_OR,
    BITWISE_AND, BITWISE_OR,
    EQUALS, NOT_EQUALS,
    GREATER_THAN, LESSER_THAN,
    GREATER_THAN_EQUALS, LESSER_THAN_EQUALS,
    SLASH, ASTERISK,
    PLUS, NEGATE,
    NOT,

    OPERATOR, LOGICAL, BITWISE, EQUALITY, RELATIONAL, BINARY_PRECEDE, BINARY, NON_COMMUTE, UNARY,

    S_OPERATOR,
    ASSIGNMENT,
    OPEN_CURVE, CLOSE_CURVE,
    OPEN_CURLY, CLOSE_CURLY,
    COMMA,

    CLASS,
    C_INT, C_BOOL, C_STRING,

    VALUE,
    ALPHA,
    E_TRUE, E_FALSE,

    V_KEYWORD, F_OUT, F_READ, LET, VAR,
    IF, ELSE, FOR, WHILE,
    FUN,

    RETURN, BREAK, CONTINUE,

    NATIVE_CALL,
    INTERRUPTION,
    NONE,
    ;

    override fun toString(): String {
        return name.lowercase(Locale.getDefault())
    }

    companion object {
        val SYMBOLS = HashMap<String, Token>()

        init {
            SYMBOLS.let {
                // FLAGS [ PRECEDENCE, .. OPERATOR ]
                it["&&"] = Token(LOGICAL_AND, arrayOf(LOGICAL, OPERATOR))
                it["||"] = Token(LOGICAL_OR, arrayOf(LOGICAL, OPERATOR))
                it["&"] = Token(BITWISE_AND, arrayOf(BITWISE, OPERATOR))
                it["|"] = Token(BITWISE_OR, arrayOf(BITWISE, OPERATOR))
                it["=="] = Token(EQUALS, arrayOf(EQUALITY, OPERATOR))
                it["!="] = Token(NOT_EQUALS, arrayOf(EQUALITY, OPERATOR))
                it[">"] = Token(GREATER_THAN, arrayOf(RELATIONAL, OPERATOR))
                it["<"] = Token(LESSER_THAN, arrayOf(RELATIONAL, OPERATOR))
                it[">="] = Token(GREATER_THAN_EQUALS, arrayOf(RELATIONAL, OPERATOR))
                it["<="] = Token(LESSER_THAN_EQUALS, arrayOf(RELATIONAL, OPERATOR))
                it["/"] = Token(SLASH, arrayOf(BINARY_PRECEDE, NON_COMMUTE, OPERATOR))
                it["*"] = Token(ASTERISK, arrayOf(BINARY_PRECEDE, NON_COMMUTE, OPERATOR))
                it["+"] = Token(PLUS, arrayOf(BINARY, OPERATOR))
                it["-"] = Token(NEGATE, arrayOf(BINARY, UNARY, OPERATOR))
                it["!"] = Token(NOT, arrayOf(UNARY))

                it["="] = Token(ASSIGNMENT, arrayOf(S_OPERATOR))

                it["("] = Token(OPEN_CURVE, arrayOf(S_OPERATOR))
                it[")"] = Token(CLOSE_CURVE, arrayOf(S_OPERATOR))
                it["{"] = Token(OPEN_CURLY, arrayOf(S_OPERATOR))
                it["}"] = Token(CLOSE_CURLY, arrayOf(S_OPERATOR))
                it[","] = Token(COMMA, arrayOf(S_OPERATOR))

                it["Int"] = Token(C_INT, arrayOf(CLASS))
                it["Bool"] = Token(C_BOOL, arrayOf(CLASS))
                it["String"] = Token(C_STRING, arrayOf(CLASS))

                it["true"] = Token(E_TRUE, arrayOf(VALUE, C_BOOL))
                it["false"] = Token(E_FALSE, arrayOf(VALUE, C_BOOL))

                it["fout"] = Token(F_OUT, arrayOf(NATIVE_CALL))
                it["fread"] = Token(F_READ, arrayOf(NATIVE_CALL))

                it["let"] = Token(LET, arrayOf(V_KEYWORD))
                it["var"] = Token(VAR, arrayOf(V_KEYWORD))

                it["if"] = Token(IF, arrayOf(NONE))
                it["else"] = Token(ELSE, arrayOf(NONE))

                it["fn"] = Token(FUN, arrayOf(NONE))

                it["return"] = Token(RETURN, arrayOf(INTERRUPTION))
                it["break"] = Token(BREAK, arrayOf(INTERRUPTION))
                it["continue"] = Token(CONTINUE, arrayOf(INTERRUPTION))
            }
        }
    }
}