package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.syntax.Token

data class Until(
    val where: Token,
    val expression: Expression,
    val body: Expression,
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.until(this)
    override fun signature() = ExpressionSignature(ExpressionType.ANY)
}