package space.themelon.eia64.compiler.expressions

import space.themelon.eia64.compiler.Expression
import space.themelon.eia64.compiler.signatures.Sign
import space.themelon.eia64.compiler.syntax.Token

data class IntLiteral(
    val where: Token,
    val value: Int
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.intLiteral(this)

    override fun sig() = Sign.INT
}