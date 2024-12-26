package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.ScopeManager
import space.themelon.eia64.runtime.Environment

data class Scope(
    val expr: Expression, // sig checked
    val imaginary: Boolean
) : Expression() {

    override fun <R> accept(v: Visitor<R>) = v.scope(this)

    override fun sig() = expr.sig()
}