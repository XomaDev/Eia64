package space.themelon.eia64.compiler.expressions

import space.themelon.eia64.compiler.Expression
import space.themelon.eia64.compiler.signatures.Sign
import space.themelon.eia64.compiler.syntax.Token

data class BoolLiteral(
    val where: Token,
    val value: Boolean
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.boolLiteral(this)

    override fun sig() = Sign.BOOL
}