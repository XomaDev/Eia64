package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature

// A simple alternative to ExpressionList, when you want to evaluate more
// than one expression without caring about their return value
data class ExpressionBind(
    val expressions: List<Expression> // sig checked
): Expression() {

    override fun <R> accept(v: Visitor<R>) = v.expressionBind(this)

    override fun sig(): Signature {
        expressions.forEach { it.sig() }
        return Sign.NONE // this does not return anything, non-consumable
    }
}