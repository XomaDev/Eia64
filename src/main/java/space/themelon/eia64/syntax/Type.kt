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
    NOT, INCREMENT, DECREMENT,
    KITA,
    DOT,

    OPERATOR, LOGICAL, BITWISE, EQUALITY, RELATIONAL, BINARY_PRECEDE, BINARY, NON_COMMUTE,
    UNARY, POSSIBLE_RIGHT_UNARY, ASSIGNMENT_TYPE,

    S_OPERATOR,
    COLON,
    ASSIGNMENT,
    ADDITIVE_ASSIGNMENT, DEDUCTIVE_ASSIGNMENT,
    MULTIPLICATIVE_ASSIGNMENT, DIVIDIVE_ASSIGNMENT,
    OPEN_CURVE, CLOSE_CURVE,
    OPEN_SQUARE, CLOSE_SQUARE,
    OPEN_CURLY, CLOSE_CURLY,
    COMMA,

    CLASS,
    E_INT, E_BOOL, E_STRING, E_CHAR,
    E_ARRAY, E_ANY, E_UNIT,

    VALUE,
    ALPHA,
    E_TRUE, E_FALSE,

    BOOL_CAST, INT_CAST, STRING_CAST,
    TYPE,

    V_KEYWORD, LET, VAR,
    IF, ELSE,
    ITR, TO, IN, BY,
    FOR, UNTIL,
    FUN,
    INCLUDE, COPY, ARRALLOC, TIME, RAND, PRINT, PRINTLN, READ, READLN, LEN, SLEEP, FORMAT,
    STDLIB,

    RETURN, BREAK, CONTINUE,

    LOOP,
    NATIVE_CALL,
    INTERRUPTION,
    NONE,
    ;

    override fun toString() = name.lowercase(Locale.getDefault())

    companion object {
        val SYMBOLS = HashMap<String, StaticToken>()

        init {
            SYMBOLS.let {
                // FLAGS [ PRECEDENCE, .. OPERATOR ]
                it["&&"] = StaticToken(LOGICAL_AND, arrayOf(LOGICAL, OPERATOR))
                it["||"] = StaticToken(LOGICAL_OR, arrayOf(LOGICAL, OPERATOR))
                it["&"] = StaticToken(BITWISE_AND, arrayOf(BITWISE, OPERATOR))
                it["|"] = StaticToken(BITWISE_OR, arrayOf(BITWISE, OPERATOR))
                it["=="] = StaticToken(EQUALS, arrayOf(EQUALITY, OPERATOR))
                it["!="] = StaticToken(NOT_EQUALS, arrayOf(EQUALITY, OPERATOR))
                it[">"] = StaticToken(GREATER_THAN, arrayOf(RELATIONAL, OPERATOR))
                it["<"] = StaticToken(LESSER_THAN, arrayOf(RELATIONAL, OPERATOR))
                it[">="] = StaticToken(GREATER_THAN_EQUALS, arrayOf(RELATIONAL, OPERATOR))
                it["<="] = StaticToken(LESSER_THAN_EQUALS, arrayOf(RELATIONAL, OPERATOR))
                it["/"] = StaticToken(SLASH, arrayOf(BINARY_PRECEDE, NON_COMMUTE, OPERATOR))
                it["*"] = StaticToken(ASTERISK, arrayOf(BINARY_PRECEDE, NON_COMMUTE, OPERATOR))
                it["+"] = StaticToken(PLUS, arrayOf(BINARY, OPERATOR))
                it["-"] = StaticToken(NEGATE, arrayOf(BINARY, UNARY, OPERATOR))
                it["!"] = StaticToken(NOT, arrayOf(UNARY))
                it["~"] = StaticToken(KITA, arrayOf(UNARY))
                it["++"] = StaticToken(INCREMENT, arrayOf(UNARY, POSSIBLE_RIGHT_UNARY))
                it["--"] = StaticToken(DECREMENT, arrayOf(UNARY, POSSIBLE_RIGHT_UNARY))

                it["."] = StaticToken(DOT)

                it["="] = StaticToken(ASSIGNMENT, arrayOf(ASSIGNMENT_TYPE, OPERATOR, S_OPERATOR))
                it["+="] = StaticToken(ADDITIVE_ASSIGNMENT, arrayOf(ASSIGNMENT_TYPE, OPERATOR, S_OPERATOR))
                it["-="] = StaticToken(DEDUCTIVE_ASSIGNMENT, arrayOf(ASSIGNMENT_TYPE, OPERATOR, S_OPERATOR))
                it["*="] = StaticToken(MULTIPLICATIVE_ASSIGNMENT, arrayOf(ASSIGNMENT_TYPE, OPERATOR, S_OPERATOR))
                it["/="] = StaticToken(DIVIDIVE_ASSIGNMENT, arrayOf(ASSIGNMENT_TYPE, OPERATOR, S_OPERATOR))
                it[":"] = StaticToken(COLON)

                it["["] = StaticToken(OPEN_SQUARE, arrayOf(S_OPERATOR))
                it["]"] = StaticToken(CLOSE_SQUARE, arrayOf(S_OPERATOR))

                it["("] = StaticToken(OPEN_CURVE, arrayOf(S_OPERATOR))
                it[")"] = StaticToken(CLOSE_CURVE, arrayOf(S_OPERATOR))
                it["{"] = StaticToken(OPEN_CURLY, arrayOf(S_OPERATOR))
                it["}"] = StaticToken(CLOSE_CURLY, arrayOf(S_OPERATOR))
                it[","] = StaticToken(COMMA, arrayOf(S_OPERATOR))

                it["Int"] = StaticToken(E_INT, arrayOf(CLASS))
                it["Bool"] = StaticToken(E_BOOL, arrayOf(CLASS))
                it["String"] = StaticToken(E_STRING, arrayOf(CLASS))
                it["Char"] = StaticToken(E_CHAR, arrayOf(CLASS))
                it["Any"] = StaticToken(E_ANY, arrayOf(CLASS))
                it["Array"] = StaticToken(E_ARRAY, arrayOf(CLASS))
                it["Unit"] = StaticToken(E_UNIT, arrayOf(CLASS))

                it["true"] = StaticToken(E_TRUE, arrayOf(VALUE, E_BOOL))
                it["false"] = StaticToken(E_FALSE, arrayOf(VALUE, E_BOOL))

                it["bool"] = StaticToken(BOOL_CAST, arrayOf(NATIVE_CALL))
                it["int"] = StaticToken(INT_CAST, arrayOf(NATIVE_CALL))
                it["str"] = StaticToken(STRING_CAST, arrayOf(NATIVE_CALL))

                it["type"] = StaticToken(TYPE, arrayOf(NATIVE_CALL))

                it["include"] = StaticToken(INCLUDE, arrayOf(NATIVE_CALL))
                it["copy"] = StaticToken(COPY, arrayOf(NATIVE_CALL))
                it["arralloc"] = StaticToken(ARRALLOC, arrayOf(NATIVE_CALL))
                it["time"] = StaticToken(TIME, arrayOf(NATIVE_CALL))
                it["rand"] = StaticToken(RAND, arrayOf(NATIVE_CALL))
                it["print"] = StaticToken(PRINT, arrayOf(NATIVE_CALL))
                it["println"] = StaticToken(PRINTLN, arrayOf(NATIVE_CALL))
                it["read"] = StaticToken(READ, arrayOf(NATIVE_CALL))
                it["readln"] = StaticToken(READLN, arrayOf(NATIVE_CALL))
                it["sleep"] = StaticToken(SLEEP, arrayOf(NATIVE_CALL))
                it["len"] = StaticToken(LEN, arrayOf(NATIVE_CALL))
                it["format"] = StaticToken(FORMAT, arrayOf(NATIVE_CALL))

                it["stdlib"] = StaticToken(STDLIB)

                it["until"] = StaticToken(UNTIL, arrayOf(LOOP))
                it["itr"] = StaticToken(ITR, arrayOf(LOOP))
                it["to"] = StaticToken(TO)
                it["in"] = StaticToken(IN)
                it["by"] = StaticToken(BY)
                it["for"] = StaticToken(FOR, arrayOf(LOOP))

                it["let"] = StaticToken(LET, arrayOf(V_KEYWORD))
                it["var"] = StaticToken(VAR, arrayOf(V_KEYWORD))

                it["if"] = StaticToken(IF, arrayOf(NONE))
                it["else"] = StaticToken(ELSE, arrayOf(NONE))

                it["fn"] = StaticToken(FUN, arrayOf(NONE))

                it["return"] = StaticToken(RETURN, arrayOf(INTERRUPTION))
                it["break"] = StaticToken(BREAK, arrayOf(INTERRUPTION))
                it["continue"] = StaticToken(CONTINUE, arrayOf(INTERRUPTION))
            }
        }
    }
}