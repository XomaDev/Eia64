package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.SimpleSignature
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.syntax.Token

data class Itr(
    val where: Token,
    val name: String,
    val from: Expression,
    val to: Expression,
    val by: Expression?,
    val body: Expression,
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.itr(this)

    override fun sig() = Sign.ANY
}