package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.ExpressionSignature
import space.themelon.eia64.analysis.VariableMetadata
import space.themelon.eia64.syntax.Token

data class Cast(
    val where: Token,
    val expr: Expression,
    val metadata: VariableMetadata
) : Expression(where) {
    override fun <R> accept(v: Visitor<R>) = expr.accept(v) // do a direct bypass, this isn't required at runtime
    override fun signature() = ExpressionSignature(metadata.runtimeType, metadata)
}