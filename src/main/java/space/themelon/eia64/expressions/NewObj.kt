package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.syntax.Token

data class NewObj(
    val where: Token,
    val name: String,
    val arguments: List<Expression>) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.new(this)
        override fun signature() = ExpressionSignature(ExpressionType.OBJECT, VariableMetadata(ExpressionType.OBJECT, name))
    }