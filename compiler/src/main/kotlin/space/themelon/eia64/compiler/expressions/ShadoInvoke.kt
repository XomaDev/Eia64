package space.themelon.eia64.compiler.expressions

import space.themelon.eia64.compiler.Expression
import space.themelon.eia64.compiler.signatures.Sign
import space.themelon.eia64.compiler.signatures.Signature
import space.themelon.eia64.compiler.syntax.Token

data class ShadoInvoke(
    val where: Token,
    val expr: Expression,
    val arguments: List<Expression>
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.unitInvoke(this)

    override fun sig(): Signature {
        // necessary
        expr.sig()
        arguments.forEach { it.sig() }
        return Sign.ANY
    }
}