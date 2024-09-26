package space.themelon.eia64.expressions

import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token

data class TypeLiteral(
    val where: Token,
    val signature: Signature
): PureLiteral(where, signature) {
    override fun <R> accept(v: Visitor<R>) = v.typeLiteral(this)

    override fun sig() = Sign.TYPE
}