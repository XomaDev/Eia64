package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.ExpressionSignature
import space.themelon.eia64.analysis.ExpressionType

data class ExpressionList(
    val expressions: List<Expression>,
    var preserveState: Boolean = false,
) : Expression(null) {

    val size = expressions.size
    override fun <R> accept(v: Visitor<R>) = v.expressions(this)
    override fun signature() = ExpressionSignature(ExpressionType.ANY)
}