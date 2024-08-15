package space.themelon.eia64.compiler.expressions

import space.themelon.eia64.compiler.Expression
import space.themelon.eia64.compiler.signatures.Sign
import space.themelon.eia64.compiler.syntax.Token

data class CharLiteral(
    val where: Token,
    val value: Char
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.charLiteral(this)

    override fun sig() = Sign.CHAR
}