package space.themelon.eia64.analysis

import space.themelon.eia64.Expression
import space.themelon.eia64.syntax.Block
import space.themelon.eia64.syntax.Flag
import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type

class ParserV2(private var block: Block) {

    private val statements = mutableListOf<Expression>()

    private val resolver = NameResolver()

    init {
        while (!block.fullyEmptied()) {
            statements.add(statement())
        }
    }

    private fun statement(): Expression {
        return expression()
    }

    private fun expression(minPrecedence: Int = 0): Expression {
        var left = element()
        // TODO: we yet have to do it
    }

    private fun element() {
        val token = peekToken()
    }

    // If the current block has reached EOF, changes to next block or throws RuntimeException
    private fun checkBlock() {
        if (block.emptied())
            block = block.next ?: throw RuntimeException("Early End of File")
    }

    private fun peekSkip(type: Type): Boolean {
        if (peekToken().type == type) {
            block.index++
            return true
        }
        return false
    }

    private fun peekToken(): Token {
        checkBlock()
        return block.peekToken()
    }

    private fun nextToken(): Token {
        checkBlock()
        return block.nextToken()
    }
}