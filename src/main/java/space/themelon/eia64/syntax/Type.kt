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
    INCLUDE, PRINT, PRINTLN, READ, READLN, LEN, SLEEP, FORMAT,
    STDLIB,

    RETURN, BREAK, CONTINUE,

    LOOP,
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
                it["~"] = Token(KITA, arrayOf(UNARY))
                it["++"] = Token(INCREMENT, arrayOf(UNARY, POSSIBLE_RIGHT_UNARY))
                it["--"] = Token(DECREMENT, arrayOf(UNARY, POSSIBLE_RIGHT_UNARY))

                it["."] = Token(DOT)

                it["="] = Token(ASSIGNMENT, arrayOf(ASSIGNMENT_TYPE, OPERATOR, S_OPERATOR))
                it["+="] = Token(ADDITIVE_ASSIGNMENT, arrayOf(ASSIGNMENT_TYPE, OPERATOR, S_OPERATOR))
                it["-="] = Token(DEDUCTIVE_ASSIGNMENT, arrayOf(ASSIGNMENT_TYPE, OPERATOR, S_OPERATOR))
                it["*="] = Token(MULTIPLICATIVE_ASSIGNMENT, arrayOf(ASSIGNMENT_TYPE, OPERATOR, S_OPERATOR))
                it["/="] = Token(DIVIDIVE_ASSIGNMENT, arrayOf(ASSIGNMENT_TYPE, OPERATOR, S_OPERATOR))
                it[":"] = Token(COLON)

                it["["] = Token(OPEN_SQUARE, arrayOf(S_OPERATOR))
                it["]"] = Token(CLOSE_SQUARE, arrayOf(S_OPERATOR))

                it["("] = Token(OPEN_CURVE, arrayOf(S_OPERATOR))
                it[")"] = Token(CLOSE_CURVE, arrayOf(S_OPERATOR))
                it["{"] = Token(OPEN_CURLY, arrayOf(S_OPERATOR))
                it["}"] = Token(CLOSE_CURLY, arrayOf(S_OPERATOR))
                it[","] = Token(COMMA, arrayOf(S_OPERATOR))

                it["Int"] = Token(E_INT, arrayOf(CLASS))
                it["Bool"] = Token(E_BOOL, arrayOf(CLASS))
                it["String"] = Token(E_STRING, arrayOf(CLASS))
                it["Char"] = Token(E_CHAR, arrayOf(CLASS))
                it["Any"] = Token(E_ANY, arrayOf(CLASS))
                it["Array"] = Token(E_ARRAY, arrayOf(CLASS))
                it["Unit"] = Token(E_UNIT, arrayOf(CLASS))

                it["true"] = Token(E_TRUE, arrayOf(VALUE, E_BOOL))
                it["false"] = Token(E_FALSE, arrayOf(VALUE, E_BOOL))

                it["bool"] = Token(BOOL_CAST, arrayOf(NATIVE_CALL))
                it["int"] = Token(INT_CAST, arrayOf(NATIVE_CALL))
                it["str"] = Token(STRING_CAST, arrayOf(NATIVE_CALL))

                it["type"] = Token(TYPE, arrayOf(NATIVE_CALL))

                it["include"] = Token(INCLUDE, arrayOf(NATIVE_CALL))
                it["print"] = Token(PRINT, arrayOf(NATIVE_CALL))
                it["println"] = Token(PRINTLN, arrayOf(NATIVE_CALL))
                it["read"] = Token(READ, arrayOf(NATIVE_CALL))
                it["readln"] = Token(READLN, arrayOf(NATIVE_CALL))
                it["sleep"] = Token(SLEEP, arrayOf(NATIVE_CALL))
                it["len"] = Token(LEN, arrayOf(NATIVE_CALL))
                it["format"] = Token(FORMAT, arrayOf(NATIVE_CALL))

                it["stdlib"] = Token(STDLIB)

                it["until"] = Token(UNTIL, arrayOf(LOOP))
                it["itr"] = Token(ITR, arrayOf(LOOP))
                it["to"] = Token(TO)
                it["in"] = Token(IN)
                it["by"] = Token(BY)
                it["for"] = Token(FOR, arrayOf(LOOP))

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