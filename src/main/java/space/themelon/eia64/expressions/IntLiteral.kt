package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.ExpressionSignature
import space.themelon.eia64.analysis.ExpressionType
import space.themelon.eia64.analysis.VariableMetadata
import space.themelon.eia64.syntax.Token

data class IntLiteral(
    val where: Token,
    val value: Int
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.intLiteral(this)
    override fun signature() = ExpressionSignature(ExpressionType.INT, VariableMetadata(ExpressionType.INT, "eint"))
}