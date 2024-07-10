package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.SimpleSignature
import space.themelon.eia64.signatures.Sign

data class ThrowExpr(
    val error: Expression
) : Expression(null) {

    override fun <R> accept(v: Visitor<R>) = v.throwExpr(this)

    override fun sig() = Sign.NONE
}