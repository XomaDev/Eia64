package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.Signature

data class ExpressionList(
    val expressions: List<Expression>,
    var preserveState: Boolean = false,
) : Expression(null) {

    val size = expressions.size
    override fun <R> accept(v: Visitor<R>) = v.expressions(this)

    override fun sig() = Signature("ExpressionList", Sign.NONE)
}