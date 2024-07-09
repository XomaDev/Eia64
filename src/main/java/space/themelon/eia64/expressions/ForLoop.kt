package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.ExpressionSignature
import space.themelon.eia64.analysis.ExpressionType
import space.themelon.eia64.syntax.Token

data class ForLoop(
    val where: Token,
    val initializer: Expression?,
    val conditional: Expression?,
    val operational: Expression?,
    val body: Expression,
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.forLoop(this)
    override fun signature() = ExpressionSignature(ExpressionType.NONE)
}