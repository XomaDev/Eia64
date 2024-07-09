package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.ExpressionSignature
import space.themelon.eia64.analysis.ExpressionType
import space.themelon.eia64.analysis.VariableMetadata
import space.themelon.eia64.syntax.Token

data class Array(
    val where: Token,
    val elements: List<Expression>
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.array(this)
    override fun signature() =
        ExpressionSignature(ExpressionType.ARRAY, VariableMetadata(ExpressionType.ARRAY, "array"))
}