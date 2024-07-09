package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.Signature
import space.themelon.eia64.syntax.Token

data class ArrayAccess(
    val where: Token,
    val expr: Expression,
    val index: Expression
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.elementAccess(this)

    override fun sig(): Signature {
        val exprSig = expr.sig()
        when (exprSig) {
            StringLiteral.SIGN -> return Signature("ArrayAccess", Sign.CHAR)
            ArrayLiteral.SIGN -> return Signature("ArrayAccess", Sign.ANY)
            else -> where.error<String>("Unknown element to perform array operation")
        }
        throw RuntimeException()
    }
}