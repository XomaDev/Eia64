package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.SimpleSignature
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token

data class When(
    val where: Token,
    val expr: Expression,
    val matches: List<Pair<Expression, Expression>>, // <Match, Body>
    val defaultBranch: Expression,
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.whenExpr(this)

    override fun sig(): Signature {
        // it checks if all the body expressions, including that of `else`
        // returns holds same signature, else it returns type Any
        val sign = defaultBranch.sig()
        for (match in matches) {
            if (sign != match.second.sig()) {
                return Sign.ANY
            }
        }
        return sign
    }
}