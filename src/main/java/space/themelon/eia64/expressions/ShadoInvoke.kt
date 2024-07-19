package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.Consumable
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.syntax.Token

data class ShadoInvoke(
    val where: Token,
    val expr: Expression,
    @Consumable("Shado invoke, arguments cannot be void") val arguments: List<Expression>
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.unitInvoke(this)

    override fun sig() = Sign.ANY
}