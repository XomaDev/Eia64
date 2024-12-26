package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.ScopeManager
import space.themelon.eia64.runtime.Environment
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.syntax.Token

data class DoubleLiteral(
    val where: Token,
    val value: Double
): Expression() {

    override fun <R> accept(v: Visitor<R>) = v.doubleLiteral(this)

    override fun sig() = Sign.DOUBLE
}