package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.ExpressionSignature
import space.themelon.eia64.syntax.Token

data class AutoVariable(
    val where: Token,
    val name: String,
    val expr: Expression
) : Expression(where) {

    init {
        signature()
    }

    override fun <R> accept(v: Visitor<R>) = v.autoVariable(this)
    override fun signature(): ExpressionSignature {
        val signature = expr.signature()
        println("SignatureX: $signature")
        return signature
    }
}