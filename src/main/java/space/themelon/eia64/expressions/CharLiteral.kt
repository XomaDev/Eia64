package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.ExpressionSignature
import space.themelon.eia64.analysis.ExpressionType
import space.themelon.eia64.syntax.Token

data class CharLiteral(
    val where: Token,
    val value: Char
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.charLiteral(this)
    override fun signature() = ExpressionSignature(ExpressionType.CHAR)
}