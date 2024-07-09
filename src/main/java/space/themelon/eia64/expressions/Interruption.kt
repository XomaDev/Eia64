package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.ExpressionSignature
import space.themelon.eia64.analysis.ExpressionType
import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type

data class Interruption(
    val where: Token,
    val operator: Type,
    val expr: Expression? = null
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.interruption(this)
    override fun signature() = expr?.signature() ?: ExpressionSignature(ExpressionType.NONE)
}