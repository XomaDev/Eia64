package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.ExpressionSignature
import space.themelon.eia64.analysis.ExpressionType
import space.themelon.eia64.syntax.Token

data class ElementAccess(
    val where: Token,
    val expr: Expression,
    val index: Expression
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.elementAccess(this)

    override fun signature(): ExpressionSignature {
        val exprType = expr.signature().type
        if (exprType == ExpressionType.STRING) return ExpressionSignature(ExpressionType.CHAR)
        if (exprType == ExpressionType.ARRAY) return ExpressionSignature(ExpressionType.ANY)
        throw RuntimeException("Unknown element expr type $exprType")
    }
}