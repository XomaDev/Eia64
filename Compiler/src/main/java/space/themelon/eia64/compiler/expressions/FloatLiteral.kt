package space.themelon.eia64.compiler.expressions

import space.themelon.eia64.compiler.Expression
import space.themelon.eia64.compiler.signatures.Sign
import space.themelon.eia64.compiler.syntax.Token

data class FloatLiteral(
    val where: Token,
    val value: Float
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.floatLiteral(this)

    override fun sig() = Sign.FLOAT
}