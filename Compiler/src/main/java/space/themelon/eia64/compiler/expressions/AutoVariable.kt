package space.themelon.eia64.compiler.expressions

import space.themelon.eia64.compiler.Expression
import space.themelon.eia64.compiler.syntax.Token

data class AutoVariable(
    val where: Token,
    val name: String,
    val expr: Expression
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.autoVariable(this)

    override fun sig() = expr.sig()
}