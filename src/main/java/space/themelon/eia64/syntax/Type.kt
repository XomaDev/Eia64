package space.themelon.eia64.syntax

import java.util.*
import kotlin.collections.HashMap

enum class Type {

    OPERATOR,
    PLUS, NEGATE, SLASH, ASTERISK,
    EQUALS, FLAG_NON_COMMUTE,

    S_OPERATOR,
    OPEN_CURVE, CLOSE_CURVE, COMMA,

    CLASS,
    C_INT, C_BOOL, C_STRING,

    VALUE,
    ALPHA,
    E_TRUE, E_FALSE,

    V_KEYWORD, LET, VAR;

    override fun toString(): String {
        return name.lowercase(Locale.getDefault())
    }

    companion object {
        val SYMBOLS = HashMap<String, Token>()

        init {
            SYMBOLS.let {
                it["+"] = Token(arrayOf(OPERATOR, PLUS))
                it["-"] = Token(arrayOf(OPERATOR, NEGATE))
                it["/"] = Token(arrayOf(OPERATOR, SLASH, FLAG_NON_COMMUTE))
                it["*"] = Token(arrayOf(OPERATOR, ASTERISK, FLAG_NON_COMMUTE))

                it["="] = Token(arrayOf(S_OPERATOR, EQUALS))
                it["("] = Token(arrayOf(S_OPERATOR, OPEN_CURVE))
                it[")"] = Token(arrayOf(S_OPERATOR, CLOSE_CURVE))
                it[","] = Token(arrayOf(S_OPERATOR, COMMA))

                it["Int"] = Token(arrayOf(CLASS, C_INT))
                it["Bool"] = Token(arrayOf(CLASS, C_BOOL))
                it["String"] = Token(arrayOf(CLASS, C_STRING))

                it["true"] = Token(arrayOf(VALUE, C_BOOL, E_TRUE))
                it["false"] = Token(arrayOf(VALUE, C_BOOL, E_FALSE))

                it["let"] = Token(arrayOf(V_KEYWORD, LET))
                it["var"] = Token(arrayOf(V_KEYWORD, VAR))
            }
        }
    }
}