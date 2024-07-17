package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.SimpleSignature
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.syntax.Token

data class ArrayLiteral(
    val where: Token,
    val elements: List<Expression>
) : Expression(where) {

    init {
        // signature verification
    }

    override fun <R> accept(v: Visitor<R>) = v.array(this)

    override fun sig() = Sign.ARRAY
}