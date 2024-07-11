package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.SimpleSignature
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type

data class UnaryOperation(
    val where: Token,
    val operator: Type,
    val expr: Expression,
    val towardsLeft: Boolean
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.unaryOperation(this)

    init {
        sig()
    }

    override fun sig(): Signature {
        val exprSign = expr.sig()
        if (towardsLeft) {
            when (operator) {
                Type.NEGATE ->
                    if (exprSign != Sign.INT) where.error<String>("Expected expression type Int for (- Negate)")

                Type.INCREMENT ->
                    if (exprSign != Sign.INT) where.error<String>("Expected expression type Int for (++ Increment)")

                Type.DECREMENT ->
                    if (exprSign != Sign.INT) where.error<String>("Expected expression type Int for (-- Decrement)")

                Type.NOT ->
                    if (exprSign != Sign.BOOL) where.error<String>("Expected expression type Bool for (! Not)")

                else -> where.error<String>("Unknown unary operator towards left")
            }
        } else {
            when (operator) {
                Type.INCREMENT ->
                    if (exprSign != Sign.INT) where.error<String>("Expected expression type Int for (++ Increment)")

                Type.DECREMENT ->
                    if (exprSign != Sign.INT) where.error<String>("Expected expression type Int for (-- Decrement)")

                else -> where.error<String>("Unknown unary operator towards left")
            }
        }
        return exprSign
    }
}