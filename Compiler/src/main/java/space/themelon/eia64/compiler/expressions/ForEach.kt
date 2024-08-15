package space.themelon.eia64.compiler.expressions

import space.themelon.eia64.compiler.Expression
import space.themelon.eia64.compiler.signatures.Sign
import space.themelon.eia64.compiler.signatures.Signature
import space.themelon.eia64.compiler.syntax.Token

    data class ForEach(
    val where: Token,
    val name: String,
    val entity: Expression, // sig checked
    val body: Expression, // sig checked
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.forEach(this)

    override fun sig(): Signature {
        entity.sig()
        body.sig()
        return Sign.NONE
    }
}