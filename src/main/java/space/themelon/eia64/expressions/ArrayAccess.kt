package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.ArrayExtension
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token

data class ArrayAccess(
    val where: Token,
    val expr: Expression,
    val index: Expression
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.arrayAccess(this)

    // Verify -> child
    override fun sig(): Signature {
        val exprSig = expr.sig()
        if (exprSig == Sign.STRING) return Sign.CHAR
        else if (exprSig is ArrayExtension) return exprSig.elementSignature
        else if (exprSig == Sign.ARRAY) return Sign.ANY
        else where.error<String>("Unknown element to perform array operation")
        throw RuntimeException()
    }
}