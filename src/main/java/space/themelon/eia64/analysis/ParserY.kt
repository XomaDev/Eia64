package space.themelon.eia64.analysis

import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type

class ParserY {
    // Not sure how this is going to turn out, im trying to imagine how it should
    // finally turn out, the language

    private lateinit var tokens: List<Token>
    private var index = 0
    private var size = 0

    fun parse(tokens: List<Token>) {
        this.tokens = tokens
        index = 0
        size = tokens.size

        //              while (!isEOF()) parseNext()
    }

    private fun parseNext() {
        // Semi outline parsing --?
        // Full outline parsing--?
    }


}