package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.Consumable
import space.themelon.eia64.signatures.SimpleSignature
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token

data class IfStatement(
    val where: Token,
    @Consumable("Condition cannot be of void expression") val condition: Expression,
    val thenBody: Expression,
    val elseBody: Expression? = null,
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.ifFunction(this)

    override fun sig(): Signature {
        val thenSig = thenBody.sig()
        // if else body is null, we cannot conclude on
        // signature, so we HAVE to return Sign NONE

        // We need to know if the `If` function is terminative or not
        if (elseBody == null) {
            // Transfer metadata from lower to upper
            return Sign.NONE.copyMetadata(thenSig)
        }
        val elseSig = elseBody.sig()
        if (thenSig == elseSig) return thenSig
        return Sign.ANY
    }
}