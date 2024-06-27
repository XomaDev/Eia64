package space.themelon.eia64.syntax

import space.themelon.eia64.Config
import space.themelon.eia64.syntax.Type.Companion.SYMBOLS
import kotlin.text.StringBuilder

class SyntaxAnalysis {

    private lateinit var source: String
    private var iterIndex = 0
    private var sourceSize: Int = 0

    private var lineCount = 0

    private lateinit var tokens: ArrayList<Token>

    fun tokenize(source: String): ArrayList<Token> {
        this.source = source
        this.iterIndex = 0
        this.sourceSize = source.length
        this.lineCount = 0
        tokens = ArrayList()

        while (!isEOF()) {
            scanTokens()
        }
        if (Config.DEBUG) {
            tokens.forEach { println(it) }
            println()
        }
        return tokens
    }

    private fun scanTokens() {
        val c = next()
        if (c == '\n') {
            lineCount++
            return
        }
        if (c == ' ') {
            return
        }
        if (c == '"') {
            parseString()
            return
        }
        if (c == '\'') {
            parseChar()
            return
        }
        back()
        var match = ""
        var token: StaticToken? = null
        while (!isEOF()) {
            token = SYMBOLS[match + peek()] ?: break
            match += next()
        }
        if (token != null) {
            tokens.add(token.normalToken(lineCount))
        } else {
            if (isAlpha(c)) {
                parseAlpha()
            } else if (isNumeric(c)) {
                parseNumeric()
            } else reportError("Unknown character '$c'")
        }
    }

    private fun parseChar() {
        var char = next()
        if (char == '\\') {
            char = when (val n = next()) {
                'n' -> '\n'
                's' -> ' '
                't' -> '\t'
                '\'', '\"', '\\' -> char
                else -> {
                    reportError("Invalid escape character '$n'")
                    '_'
                }
            }
        }
        if (next() != '\'')
            reportError("Invalid syntax while using single quotes")
        tokens.add(Token(lineCount, Type.E_CHAR, arrayOf(Type.VALUE), char))
    }

    private fun parseString() {
        val content = StringBuilder()
        while (!isEOF()) {
            var c = next()
            if (c == '\"') break
            else if (c == '\\') {
                when (val e = next()) {
                    'n' -> c = '\n'
                    't' -> c = '\t'
                    's' -> c = ' '
                    '\'', '\"', '\\' -> break
                    else -> reportError("Invalid escape character '$e'")
                }
            }
            content.append(c)
        }
        tokens.add(Token(lineCount, Type.E_STRING, arrayOf(Type.VALUE), content.toString()))
    }

    private fun parseAlpha() {
        val content = StringBuilder()
        while (!isEOF()) {
            val p = peek()
            if (isAlpha(p)) {
                content.append(p)
                skip()
            } else break
        }
        val value = content.toString()
        val token = SYMBOLS[value]
        if (token != null) {
            tokens.add(token.normalToken(lineCount,))
        } else {
            tokens.add(Token(lineCount, Type.ALPHA, arrayOf(Type.VALUE), value))
        }
    }

    private fun parseNumeric() {
        val content = StringBuilder()
        while (!isEOF()) {
            val p = peek()
            if (isNumeric(p)) {
                content.append(p)
                skip()
            } else break
        }
        tokens.add(Token(lineCount, Type.E_INT, arrayOf(Type.VALUE), content.toString().toInt()))
    }

    private fun reportError(message: String) {
        throw RuntimeException("[line $lineCount] $message")
    }

    private fun isNumeric(c: Char) = c in '0'..'9'
    private fun isAlpha(c: Char) = (c in 'a'..'z' || c in 'A'..'Z') || c == '_'

    private fun back() {
        iterIndex--
    }

    private fun skip() {
        iterIndex++
    }

    private fun next() = source[iterIndex++]
    private fun peek() = source[iterIndex]
    private fun isEOF() = iterIndex == sourceSize
}