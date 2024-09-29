package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.Sign

data class ImportJavaClass(
    val clazz: Class<*>,
    val namedAs: String,
): Expression() {
    override fun <R> accept(v: Visitor<R>) = v.importJava(this)

    override fun sig() = Sign.NONE
}
