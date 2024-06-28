package space.themelon.eia64.syntax

import space.themelon.eia64.syntax.Type.*

class Lexer(private val source: String) {

    private class Metadata(val type: Type, vararg val flags: Type)

    companion object {
        private val characterMapping = mapOf(
            Pair("=", Metadata(ASSIGNMENT, ASSIGNMENT_TYPE, OPERATOR, S_OPERATOR)),
            Pair("+=", Metadata(ADDITIVE_ASSIGNMENT, ASSIGNMENT_TYPE, OPERATOR, S_OPERATOR)),
            Pair("-=", Metadata(DEDUCTIVE_ASSIGNMENT, ASSIGNMENT_TYPE, OPERATOR, S_OPERATOR)),
            Pair("*=", Metadata(MULTIPLICATIVE_ASSIGNMENT, ASSIGNMENT_TYPE, OPERATOR, S_OPERATOR)),
            Pair("/=", Metadata(DIVIDIVE_ASSIGNMENT, ASSIGNMENT_TYPE, OPERATOR, S_OPERATOR)),

            Pair("||", Metadata(LOGICAL_OR, LOGICAL_OR, OPERATOR)),
            Pair("&&", Metadata(LOGICAL_AND, LOGICAL_AND, OPERATOR)),

            Pair("==", Metadata(EQUALS, EQUALITY, OPERATOR)),
            Pair("!=", Metadata(NOT_EQUALS, EQUALITY, OPERATOR)),

            Pair(">", Metadata(GREATER_THAN, RELATIONAL, OPERATOR)),
            Pair("<", Metadata(LESSER_THAN, RELATIONAL, OPERATOR)),
            Pair(">=", Metadata(GREATER_THAN_EQUALS, RELATIONAL, OPERATOR)),
            Pair("<=", Metadata(LESSER_THAN_EQUALS, RELATIONAL, OPERATOR)),

            // TODO:
            //   cleanup a lot of stuff
            Pair("*", Metadata(ASTERISK, BINARY_PRECEDE, NON_COMMUTE, OPERATOR)),
            Pair("/", Metadata(SLASH, BINARY_PRECEDE, NON_COMMUTE, OPERATOR)),

            Pair("+", Metadata(PLUS, BINARY, OPERATOR)),
            Pair("-", Metadata(NEGATE, BINARY, UNARY, OPERATOR)),

            Pair("!", Metadata(NOT, UNARY)),
            Pair("~", Metadata(KITA, UNARY)),


        )

    }

    private var index = 0
    private var line = 1

    val tokens = ArrayList<Token>()

    init {
        while (!isEOF()) parseNext()
    }

    private fun parseNext() {
        val char = next()
        tokens.add(when (char) {
            '=' -> if (consumeNext('=')) createToken(EQUALS, arrayOf(EQUALITY, OPERATOR))
                   else createToken(ASSIGNMENT, arrayOf(ASSIGNMENT_TYPE, OPERATOR, S_OPERATOR))

            '*' -> if (consumeNext('=')) createToken(MULTIPLICATIVE_ASSIGNMENT, arrayOf(ASSIGNMENT_TYPE, OPERATOR, S_OPERATOR))
                   else createToken(ASTERISK, arrayOf(BINARY_PRECEDE, NON_COMMUTE, OPERATOR))

            '/' -> if (consumeNext('=')) createToken(DIVIDIVE_ASSIGNMENT, arrayOf(ASSIGNMENT_TYPE, OPERATOR, S_OPERATOR))
                   else createToken(SLASH, arrayOf(BINARY_PRECEDE, NON_COMMUTE, OPERATOR))

            '+' ->
                if (consumeNext('+')) createToken(INCREMENT, arrayOf(UNARY, POSSIBLE_RIGHT_UNARY))
                else createToken(PLUS, arrayOf(BINARY, OPERATOR))
            '-' ->
                if (consumeNext('-')) createToken(DECREMENT, arrayOf(UNARY, POSSIBLE_RIGHT_UNARY))
                else createToken(NEGATE, arrayOf(BINARY, UNARY, OPERATOR))

            '!' -> createToken(NOT, arrayOf(UNARY))
            '~' -> createToken(KITA, arrayOf(UNARY))

            else -> throw RuntimeException("Unknown operator at line $line: '$char'")
        })
    }

    private fun createToken(type: Type, flags: Array<Type>) = Token(line, type, flags)

    private fun isEOF() = index == source.length

    private fun consumeNext(char: Char): Boolean {
        if (isEOF()) return false
        val match = peek() == char
        if (match) index++
        return match
    }

    private fun next(): Char {
        if (isEOF()) throw RuntimeException("Early EOF at line $line")
        return source[index++]
    }

    private fun peek(): Char {
        if (isEOF()) throw RuntimeException("Early EOF at line $line")
        return source[index]
    }
}