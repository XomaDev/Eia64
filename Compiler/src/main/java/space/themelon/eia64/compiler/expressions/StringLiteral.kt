package space.themelon.eia64.compiler.expressions

import space.themelon.eia64.compiler.Expression
import space.themelon.eia64.compiler.signatures.Sign
import space.themelon.eia64.compiler.syntax.Token

data class StringLiteral(
    val where: Token,
    val value: String
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.stringLiteral(this)

    override fun sig() = Sign.STRING
}
