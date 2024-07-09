package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.ExpressionSignature
import space.themelon.eia64.analysis.ExpressionType

data class Include(
    val names: List<String>
) : Expression(null) {

    override fun <R> accept(v: Visitor<R>): R = v.include(this)
    override fun signature() = ExpressionSignature(ExpressionType.NONE)
}