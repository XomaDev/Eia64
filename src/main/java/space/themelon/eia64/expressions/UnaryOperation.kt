package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type

data class UnaryOperation(
    val where: Token,
    val operator: Type,
    val expr: Expression, // sig checked
    val towardsLeft: Boolean
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.unaryOperation(this)

    override fun sig(): Signature {
        val exprSign = expr.sig()
        val exprSignLog = exprSign.logName()
        // TODO:
        //  In future we need to notify user the wrong signature received
        if (towardsLeft) {
            when (operator) {
                Type.NEGATE ->
                    if (!exprSign.isNumeric()) where.error<String>("Expected Numeric expression type for (- Negate), got $exprSignLog")

                Type.INCREMENT ->
                    if (!exprSign.isNumeric()) where.error<String>("Expected Numeric expression type for (++ Increment), got $exprSignLog")

                Type.DECREMENT ->
                    if (!exprSign.isNumeric()) where.error<String>("Expected Numeric expression type for (-- Decrement), got $exprSignLog")

                Type.NOT ->
                    if (exprSign != Sign.BOOL) where.error<String>("Expected Bool expression type for (! Not), got $exprSignLog")

                else -> where.error<String>("Unknown unary operator towards left")
            }
        } else {
            when (operator) {
                Type.INCREMENT ->
                    if (!exprSign.isNumeric()) where.error<String>("Expected expression type Numeric for (++ Increment), got $exprSignLog")

                Type.DECREMENT ->
                    if (!exprSign.isNumeric()) where.error<String>("Expected expression type Numeric for (-- Decrement), got $exprSignLog")

                else -> where.error<String>("Unknown unary operator towards left")
            }
        }
        return exprSign
    }
}