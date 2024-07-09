package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.Signature
import space.themelon.eia64.syntax.Token

data class ClassMethodCall(
    val where: Token,
    val static: Boolean,
    val obj: Expression,
    val method: String,
    val arguments: List<Expression>,
    val sign: String
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.classMethodCall(this)

    override fun sig() = Signature("CMC", sign)
}