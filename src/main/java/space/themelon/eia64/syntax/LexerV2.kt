package space.themelon.eia64.syntax

import space.themelon.eia64.syntax.Type.*
import space.themelon.eia64.syntax.Type.Companion.KEYWORDS

class LexerV2(private var source: String) {

    companion object {
        private fun isNumeric(c: Char) = c in '0'..'9'
        private fun isAlpha(c: Char) = (c in 'a'..'z' || c in 'A'..'Z') || c == '_'
    }

    val headBlock = Block(1)
    private var currentBlock = headBlock

    private var line = 1
    private var index = 0

    init {
        if (source.isEmpty())
            throw RuntimeException("Source cannot be empty")
        while (!isEOF()) {
            lex()
        }
    }

    private fun lex() {
        val char = next()
        if (char == '\n') {
            line++
            currentBlock.completed = true
            return
        }
        if (char == ' ' || char == ' ') return
        if (char == ';') {
            while (!isEOF() && peek() != '\n') index++
            return
        }
        pushToken(when (char) {
            '=' -> if (consumeNext('=')) createOp("==") else createOp("=")

            '*' -> if (consumeNext('=')) createOp("*=") else createOp("*")
            '/' -> if (consumeNext('=')) createOp("/=") else createOp("/")

            '+' ->
                if (consumeNext('+')) createOp("++")
                else if (consumeNext('=')) createOp("+=")
                else createOp("+")
            '-' -> if (consumeNext('-')) createOp("--")
            else if (consumeNext('=')) createOp("-=")
            else if (consumeNext('>')) createOp("->")
            else createOp("-")

            '|' -> if (consumeNext('|')) createOp("||") else createOp("|")
            '&' -> if (consumeNext('&')) createOp("&&") else createOp("&")

            '!' -> if (consumeNext('=')) createOp("!=") else createOp("!")

            '>' -> if (consumeNext('=')) createOp(">=") else createOp(">")
            '<' -> if (consumeNext('=')) createOp("<=") else createOp("<")

            '.' -> createOp(".")
            ':' -> if (consumeNext('=')) createOp(":=") else createOp(":")
            ',' -> createOp(",")
            '(' -> createOp("(")
            ')' -> createOp(")")
            '[' -> createOp("[")
            ']' -> createOp("]")
            '{' -> createOp("{")
            '}' -> createOp("}")
            '\'' -> parseChar()
            '"' -> parseString()
            else -> {
                if (isAlpha(char)) parseAlpha(char)
                else if (isNumeric(char)) parseNumeric(char)
                else throw RuntimeException("Unknown operator at line $line: '$char'")
            }
        })
    }

    private fun pushToken(token: Token) {
        if (currentBlock.completed) {
            val newBlock = Block(line)
            currentBlock.next = newBlock
            currentBlock = newBlock
        }
        currentBlock.list.add(token)
    }

    private fun createOp(operator: String): Token {
        val op = Type.SYMBOLS[operator] ?: throw RuntimeException("Cannot find operator '$operator'")
        return op.normalToken(line)
    }

    private fun parseChar(): Token {
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
        return Token(line, E_CHAR, arrayOf(Flag.VALUE), char)
    }

    private fun parseString(): Token {
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
        return Token(line, E_STRING, arrayOf(Flag.VALUE), content.toString())
    }

    private fun parseAlpha(c: Char): Token {
        val content = StringBuilder()
        content.append(c)
        while (!isEOF()) {
            val p = peek()
            if (isAlpha(p) || isNumeric(p)) {
                content.append(next())
            } else break
        }
        val value = content.toString()
        val token = KEYWORDS[value]
        return token?.normalToken(line) ?: Token(line, ALPHA, arrayOf(Flag.VALUE), value)
    }

    private fun parseNumeric(c: Char): Token {
        val content = StringBuilder()
        content.append(c)
        while (!isEOF()) {
            val p = peek()
            if (isNumeric(p)) {
                content.append(next())
            } else break
        }
        return Token(line, E_INT, arrayOf(Flag.VALUE), content.toString().toInt())
    }

    private fun isEOF() = index == source.length

    private fun next(): Char {
        if (isEOF()) throw RuntimeException("Early EOF at line $line")
        return source[index++]
    }

    private fun peek(): Char {
        if (isEOF()) throw RuntimeException("Early EOF at line $line")
        return source[index]
    }

    private fun consumeNext(char: Char): Boolean {
        if (isEOF()) return false
        val match = peek() == char
        if (match) index++
        return match
    }

    private fun reportError(message: String) {
        throw RuntimeException("[line $line] $message")
    }
}