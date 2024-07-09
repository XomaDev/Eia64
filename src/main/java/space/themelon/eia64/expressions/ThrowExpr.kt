package space.themelon.eia64.expressions

import space.themelon.eia64.Expression

data class ThrowExpr(
    val error: Expression
) : Expression(null) {

    override fun <R> accept(v: Visitor<R>) = v.throwExpr(this)
    override fun signature() = ExpressionSignature(ExpressionType.NONE)
}