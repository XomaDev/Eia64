package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token
import java.lang.reflect.Field

data class JavaField(
    val where: Token,
    val jObject: Expression,
    val field: Field,
    val fieldSignature: Signature
): Expression() {

    override fun <R> accept(v: Visitor<R>) = v.javaFieldAccess(this)

    override fun sig() = fieldSignature
}