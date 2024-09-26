package space.themelon.eia64.expressions

import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.syntax.Token

data class IntLiteral(
    val where: Token,
    override val value: Int
) : PureLiteral(where, value) {

    override fun <R> accept(v: Visitor<R>) = v.intLiteral(this)

    override fun sig() = Sign.INT
}