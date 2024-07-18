package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.SimpleSignature
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type
import kotlin.math.exp

data class Interruption(
    val where: Token,
    val operator: Type,
    val expr: Expression? = null
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.interruption(this)

    override fun sig(): Signature {
        if (operator == Type.RETURN || operator == Type.USE) {
            if (expr == null) where.error<String>("No expression for operator $operator provided")
            val signature = expr!!.sig()
            if (operator == Type.RETURN) {
                signature.returnMetadata = signature
            }
            return signature
        }
        return Sign.ANY
    }
}