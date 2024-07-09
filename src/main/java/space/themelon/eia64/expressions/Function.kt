package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.ExpressionSignature
import space.themelon.eia64.analysis.ExpressionType
import space.themelon.eia64.analysis.ValueDefinition
import space.themelon.eia64.syntax.Type

data class Function(
    val name: String,
    val arguments: List<ValueDefinition>,
    val returnType: Type,
    val body: Expression
) : Expression(null) {

    override fun <R> accept(v: Visitor<R>) = v.function(this)
    override fun signature() = ExpressionSignature(ExpressionType.NONE) // this is just a pure declration
}