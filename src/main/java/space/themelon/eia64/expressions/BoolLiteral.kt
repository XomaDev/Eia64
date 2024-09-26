package space.themelon.eia64.expressions

import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.syntax.Token

data class BoolLiteral(
    val where: Token,
    override val value: Boolean
) : PureLiteral(where, value) {

    override fun <R> accept(v: Visitor<R>) = v.boolLiteral(this)

    override fun sig() = Sign.BOOL
}