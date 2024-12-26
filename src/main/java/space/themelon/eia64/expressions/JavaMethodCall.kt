package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token
import java.lang.reflect.Method

data class JavaMethodCall(
    val where: Token,
    val jObject: Expression,
    val method: Method,
    val arguments: List<Expression>,
    val callSignature: Signature,
) : Expression() {

    override fun <R> accept(v: Visitor<R>) = v.javaMethodCall(this)

    override fun sig(): Signature {
        arguments.forEach { it.sig() }
        return callSignature
    }
}