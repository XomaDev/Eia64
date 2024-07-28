package space.themelon.eia64.syntax

class LiveUnit {

    private val tokens = ArrayList<Token>()

    fun feedLine(line: String) {
        val newTokens = Lexer(line).tokens
        tokens += newTokens
    }
}