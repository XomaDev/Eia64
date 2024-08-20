package space.themelon.eia64.compiler.expressions

import space.themelon.eia64.compiler.Expression
import space.themelon.eia64.compiler.signatures.Sign
import space.themelon.eia64.compiler.signatures.Signature
import space.themelon.eia64.compiler.syntax.Token
import space.themelon.eia64.compiler.syntax.Type

data class UnaryOperation(
    val where: Token,
    val operator: Type,
    val expr: Expression, // sig checked
    val towardsLeft: Boolean
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.unaryOperation(this)

    override fun sig(): Signature {
        val exprSign = expr.sig()
        if (towardsLeft) {
            when (operator) {
                Type.NEGATE -> if (!exprSign.isNumeric()) applyError("Numeric", "- Negate")
                Type.INCREMENT -> if (!exprSign.isNumeric()) applyError("Numeric", "++ Increment")
                Type.DECREMENT -> if (!exprSign.isNumeric()) applyError("Numeric", "-- Decrement")
                Type.EXCLAMATION -> if (exprSign != Sign.BOOL) applyError("Bool", "! Not")
                else -> where.error<String>("Unknown unary operator towards left")
            }
        } else {
            when (operator) {
                Type.INCREMENT -> if (!exprSign.isNumeric()) applyError("Numeric", "++ Increment")
                Type.DECREMENT -> if (!exprSign.isNumeric()) applyError("Numeric", "-- Decrement")
                else -> where.error<String>("Unknown unary operator towards left")
            }
        }
        return exprSign
    }

    private fun applyError(type: String, operator: String) {
        where.error<String>("Expected $type expression for ($operator) but got ${expr.sig().logName()}")
    }
}