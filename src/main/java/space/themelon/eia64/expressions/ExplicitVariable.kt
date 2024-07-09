package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.Signature
import space.themelon.eia64.syntax.Token

data class ExplicitVariable(
    val where: Token,
    val mutable: Boolean,
    val name: String,
    val expr: Expression,
    val explicitSign: String
) : Expression(where) {

    init {
        sig()
    }

    override fun <R> accept(v: Visitor<R>) = v.variable(this)

    override fun sig(): Signature {
        val exprSig = expr.sig()
        if (explicitSign != exprSig.signature) {
            where.error<String>("Variable '$name' expected signature $explicitSign but got ${exprSig.signature}")
            throw RuntimeException()
        }
        return Signature("ExplicitVariable", explicitSign)
    }
}