package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.Signature
import space.themelon.eia64.syntax.Token

data class IfStatement(
    val where: Token,
    val condition: Expression,
    val thenBody: Expression,
    val elseBody: Expression? = null,
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.ifFunction(this)

    override fun sig(): Signature {
        val thenSig = thenBody.sig()
        if (elseBody == null) return thenSig.holderCopy("IfStatement")
        val elseSig = elseBody.sig()
        if (thenSig == elseSig) return thenSig.holderCopy("IfStatement")
        return Signature("IfStatement", Sign.ANY)
    }
}