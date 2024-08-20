package space.themelon.eia64.compiler.expressions

import space.themelon.eia64.compiler.Expression
import space.themelon.eia64.compiler.signatures.Sign
import space.themelon.eia64.compiler.signatures.Signature
import space.themelon.eia64.compiler.syntax.Token

data class ThrowExpr(
    val where: Token,
    val error: Expression // sig checked
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.throwExpr(this)

    override fun sig(): Signature {
        error.sig() // necessary
        return Sign.NONE
    }
}