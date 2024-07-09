package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.ExpressionSignature
import space.themelon.eia64.analysis.ExpressionType
import space.themelon.eia64.syntax.Token

data class If(
    val where: Token,
    val condition: Expression,
    val thenBody: Expression,
    val elseBody: Expression? = null,
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.ifFunction(this)

    override fun signature(): ExpressionSignature {
        val thenType = thenBody.signature()
        if (elseBody == null) return thenType

        val elseType = elseBody.signature()
        if (thenType == elseType) return thenType
        return ExpressionSignature(ExpressionType.ANY)
    }
}