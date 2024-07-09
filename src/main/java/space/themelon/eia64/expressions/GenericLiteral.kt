package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.ExpressionSignature
import space.themelon.eia64.analysis.ExpressionType
import space.themelon.eia64.syntax.Token

// for internal evaluation use
data class GenericLiteral(
    val where: Token,
    val value: Any
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.genericLiteral(this)
    override fun signature() = ExpressionSignature(ExpressionType.ANY)
}