package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.Signature
import space.themelon.eia64.syntax.Token

data class CharLiteral(
    val where: Token,
    val value: Char
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.charLiteral(this)

    override fun sig() = SIGN

    companion object {
        private val SIGN = Signature("CharLiteral", Sign.CHAR)
    }
}