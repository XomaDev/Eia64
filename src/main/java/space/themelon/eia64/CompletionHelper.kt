package space.themelon.eia64

import space.themelon.eia64.syntax.Lexer
import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type
import java.util.StringJoiner

// This helps us decide when to submit the code for execution
//  While operating in the live shell environment
class CompletionHelper(
    val ready: (tokens: List<Token>) -> Unit,
    val syntaxError: (String) -> Unit,
) {

    private var buffer = StringJoiner("\n")

    fun addLine(line: String): Boolean {
        buffer.add(line)
        val tokens = try {
            Lexer(buffer.toString()).tokens
        } catch (e: Exception) {
            syntaxError(e.message.toString())
            return false
        }
        var entitiesOpen = 0
        tokens.forEach {
            if (it.type == Type.OPEN_CURVE || it.type == Type.OPEN_SQUARE || it.type == Type.OPEN_CURLY) entitiesOpen++
            else if (it.type == Type.CLOSE_CURVE || it.type == Type.CLOSE_SQUARE || it.type == Type.CLOSE_CURLY) entitiesOpen--
            else { }
        }
        if (entitiesOpen == 0) {
            buffer = StringJoiner("\n")
            ready(tokens)
            return true
        }
        return false
    }
}