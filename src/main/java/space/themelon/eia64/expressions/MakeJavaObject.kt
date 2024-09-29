package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature

data class MakeJavaObject(
    val clazz: Class<*>,
    val debugName: String,
    val arguments: List<Expression>,
): Expression() {
    override fun <R> accept(v: Visitor<R>) = v.makeJavaObject(this)

    override fun sig(): Signature {
        arguments.forEach { it.sig() }
        return Sign.JAVA
    }
}