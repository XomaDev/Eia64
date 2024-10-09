package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature

data class JavaMethodCall(
    val objectExpr: Expression,
    val funcName: String,
    val parameters: List<Expression>,
): Expression() {

    override fun <R> accept(v: Visitor<R>) = v.javaMethodCall(this)

    override fun sig(): Signature {
        objectExpr.sig()
        parameters.forEach { it.sig() }
        return Sign.JAVA
    }
}