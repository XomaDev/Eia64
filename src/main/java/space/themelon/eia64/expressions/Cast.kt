package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.ScopeManager
import space.themelon.eia64.runtime.Environment
import space.themelon.eia64.signatures.*
import space.themelon.eia64.syntax.Token

data class Cast(
    val where: Token,
    val expr: Expression,
    val expectSignature: Signature
) : Expression() {

    // we actually do require evaluating this node at runtime
    override fun <R> accept(v: Visitor<R>) = v.cast(this)

    override fun sig(): Signature {
        return expectSignature
    }
}