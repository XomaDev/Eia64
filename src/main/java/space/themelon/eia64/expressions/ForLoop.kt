package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.Consumable
import space.themelon.eia64.signatures.SimpleSignature
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.syntax.Token

data class ForLoop(
    val where: Token,
    @Consumable("For initializer cannot be void expression") val initializer: Expression?,
    @Consumable("For conditional cannot be void expression") val conditional: Expression?,
    @Consumable("For operational cannot be void expression") val operational: Expression?,
    val body: Expression,
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.forLoop(this)

    override fun sig() = Sign.NONE
}