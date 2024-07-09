package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.ExpressionSignature
import space.themelon.eia64.analysis.ExpressionType

data class Shadow(
    val names: List<String>,
    val body: Expression
) : Expression() {

    override fun <R> accept(v: Visitor<R>) = v.shado(this)
    override fun signature() = ExpressionSignature(ExpressionType.ANY)
}