package space.themelon.eia64.expressions

import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.syntax.Token

data class StringLiteral(
    val where: Token,
    override val value: String
) : PureLiteral(where, value) {

    override fun <R> accept(v: Visitor<R>) = v.stringLiteral(this)

    override fun sig() = Sign.STRING
}
