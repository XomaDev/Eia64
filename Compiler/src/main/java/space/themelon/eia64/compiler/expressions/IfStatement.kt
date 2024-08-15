package space.themelon.eia64.compiler.expressions

import space.themelon.eia64.compiler.Expression
import space.themelon.eia64.compiler.signatures.Sign
import space.themelon.eia64.compiler.signatures.Signature
import space.themelon.eia64.compiler.syntax.Token

data class IfStatement(
    val where: Token,
    val condition: Expression, // sig checked
    val thenBody: Expression, // sig checked
    val elseBody: Expression, // sig checked
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.ifFunction(this)

    override fun sig(): Signature {
        condition.sig() // necessary

        val thenSig = thenBody.sig()
        // if else body is null, we cannot conclude on
        // signature, so we HAVE to return Sign NONE

        // We need to know if the `If` function is terminative or not
        if (elseBody is NoneExpression) {
            // Transfer metadata from lower to upper
            return Sign.NONE
        }
        val elseSig = elseBody.sig()
        if (thenSig == elseSig) return thenSig
        return Sign.ANY
    }
}