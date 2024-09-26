package space.themelon.eia64.expressions

import space.themelon.eia64.Expression

data class Annotated(
    val name: String,
    val actual: Expression,
    val properties: HashMap<String, PureLiteral>
): Expression() {

    override fun <R> accept(v: Visitor<R>) = actual.accept(v)
    override fun sig() = actual.sig()
}