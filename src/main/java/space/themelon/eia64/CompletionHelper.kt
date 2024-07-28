package space.themelon.eia64

import space.themelon.eia64.syntax.Lexer
import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type
import java.util.StringJoiner

// This helps us decide when to submit the code for execution
//  While operating in the live shell environment
class CompletionHelper {

    private val buffer = StringJoiner("\n")
    private var ready = false // are we ready for execution?

    private var entitiesOpen = 0

    fun addLine(line: String): List<Token>? {
        buffer.add(line)
        val tokens = Lexer(buffer.toString()).tokens
        tokens.forEach { analyse(it) }
        if (ready) {
            ready = false
            return tokens
        }
        // We are not yet ready, there's some more code
        return null
    }

    fun pending() = entitiesOpen > 0

    private fun analyse(token: Token) {
        when (token.type) {
            Type.OPEN_CURVE, Type.OPEN_SQUARE, Type.OPEN_CURLY -> entitiesOpen++
            Type.CLOSE_CURVE, Type.CLOSE_SQUARE -> entitiesOpen--
            else -> { }
        }
        if (entitiesOpen == 0) ready = true
    }
}