package space.themelon.eia64.compiler.expressions

import space.themelon.eia64.compiler.Expression
import space.themelon.eia64.compiler.signatures.Sign
import space.themelon.eia64.compiler.signatures.Signature
import space.themelon.eia64.compiler.syntax.Token

data class Itr(
    val where: Token,
    val name: String,
    val from: Expression, // sig checked
    val to: Expression, // sig checked
    val by: Expression?, // sig checked
    val body: Expression, // sig checked
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.itr(this)

    override fun sig(): Signature {
        from.sig()
        to.sig()
        by?.sig()
        body.sig()
        return Sign.ANY
    }
}