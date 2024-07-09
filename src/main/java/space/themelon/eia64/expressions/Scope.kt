package space.themelon.eia64.expressions

import space.themelon.eia64.Expression

data class Scope(
    val expr: Expression,
    val imaginary: Boolean
) : Expression(null) {

    override fun <R> accept(v: Visitor<R>) = v.scope(this)

    // TODO:
    //  in future we need to check this again, if we are doing it right
    override fun signature() = expr.signature()
}