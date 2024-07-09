package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.Signature
import space.themelon.eia64.syntax.Token

data class ArrayLiteral(
    val where: Token,
    val elements: List<Expression>
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.array(this)

    override fun sig() = SIGN

    companion object {
        val SIGN = Signature("ArrayLiteral", Sign.ARRAY)
    }
}