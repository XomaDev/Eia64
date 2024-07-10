package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.SimpleSignature
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.syntax.Token

data class ArrayAccess(
    val where: Token,
    val expr: Expression,
    val index: Expression
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.elementAccess(this)

    override fun sig(): SimpleSignature {
        val exprSig = expr.sig()
        when (exprSig) {
            Sign.STRING -> return Sign.CHAR
            Sign.ARRAY -> return Sign.ANY
            else -> where.error<String>("Unknown element to perform array operation")
        }
        throw RuntimeException()
    }
}