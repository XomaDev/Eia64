package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature

data class JavaMethodCall(
    val objectExpression: Expression,
    val methodName: String,
    val args: List<Expression>,
): Expression() {

    override fun <R> accept(v: Visitor<R>) = v.javaMethodCall(this)

    override fun sig(): Signature {
        objectExpression.sig()
        args.forEach { it.sig() }
        return Sign.JAVA
    }
}