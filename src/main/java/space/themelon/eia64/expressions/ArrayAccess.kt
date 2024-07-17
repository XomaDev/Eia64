package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.ArraySignature
import space.themelon.eia64.signatures.SimpleSignature
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token

data class ArrayAccess(
    val where: Token,
    val expr: Expression,
    val index: Expression
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.arrayAccess(this)

    override fun sig(): Signature {
        when (val exprSig = expr.sig()) {
            Sign.STRING -> return Sign.CHAR
            is ArraySignature -> return exprSig.elementSignature
            else -> where.error<String>("Unknown element to perform array operation")
        }
        throw RuntimeException()
    }
}