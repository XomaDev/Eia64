package space.themelon.eia64.compiler.expressions

import space.themelon.eia64.compiler.Expression
import space.themelon.eia64.compiler.signatures.Sign
import space.themelon.eia64.compiler.signatures.Signature
import space.themelon.eia64.compiler.syntax.Token

data class When(
    val where: Token,
    val expr: Expression,
    val matches: List<Pair<Expression, Expression>>, // <Match, Body>, sig checked
    val defaultBranch: Expression,
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.whenExpr(this)

    override fun sig(): Signature {
        // necessary
        matches.forEach { it.first.sig(); it.second.sig() }

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