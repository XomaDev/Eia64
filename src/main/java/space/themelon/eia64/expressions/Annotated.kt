package space.themelon.eia64.expressions

import space.themelon.eia64.Expression

data class Annotated(
    val name: String,
    private val actual: Expression,
): Expression() {

    override fun <R> accept(v: Visitor<R>) = actual.accept(v)
    override fun sig() = actual.sig()
}