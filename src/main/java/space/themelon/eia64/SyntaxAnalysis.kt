package space.themelon.eia64

import java.io.File
import kotlin.text.StringBuilder

class SyntaxAnalysis(file: File) {

    private val symbols = HashMap<String, List<String>>()

    init {
        file.readLines().forEach { line ->
            line.trim().split(" ").let {
                symbols[it[0]] = it[1].split(":")
            }
        }
    }

    private lateinit var source: String
    private var iterIndex = 0
    private var sourceSize: Int = 0

    private lateinit var tokens: ArrayList<Token>

    fun tokenize(source: String) {
        this.source = source
        this.iterIndex = 0
        this.sourceSize = source.length
        tokens = ArrayList()

        while (!isEOF()) {
            scanTokens()
        }
        tokens.forEach { println(it) }
    }

    private fun scanTokens() {
        val c = next()
        if (c == ' ' || c == '\n') {
            return
        }
        if (c == '"') {
            parseString()
            return
        }
        back()
        var match = ""
        var types: List<String>? = null
        while (!isEOF()) {
            types = symbols[match + peek()] ?: break
            match += next()
        }
        if (match.isNotEmpty()) {
            tokens.add(Token(types!!, match))
        } else {
            if (isAlpha(c)) {
                parseAlpha()
            } else if (isNumeric(c)) {
                parseNumeric()
            } else throw RuntimeException("Unknown character '$c'")
        }
    }

    private fun parseString() {
        val content = StringBuilder()
        while (!isEOF()) {
            val c = next()
            if (c == '"') {
                break
            }
            content.append(c)
        }
        tokens.add(Token(STRING_TYPE, content.toString()))
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
        val symbol = content.toString()
        tokens.add(Token(symbols[symbol] ?: ALPHA, symbol))
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
        tokens.add(Token(NUMBER_TYPE, content.toString()))
    }

    private fun back() {
        iterIndex--
    }

    private fun skip() {
        iterIndex++
    }

    private fun isNumeric(c: Char) = c in '0'..'9'
    private fun isAlpha(c: Char) = c in 'a'..'z' || c in 'A'..'Z'

    private fun next() = source[iterIndex++]
    private fun peek() = source[iterIndex]
    private fun isEOF() = iterIndex == sourceSize

    companion object {
        private val NUMBER_TYPE = listOf("Number", "Value")
        private val STRING_TYPE = listOf("String", "Value")
        private val ALPHA = listOf("Identifier", "Value")
    }
}