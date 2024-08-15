package space.themelon.eia64.compiler.expressions

import space.themelon.eia64.compiler.Expression
import space.themelon.eia64.compiler.signatures.Sign
import space.themelon.eia64.compiler.signatures.Signature
import space.themelon.eia64.compiler.syntax.Token

data class ForLoop(
    val where: Token,
    val initializer: Expression?, // sig checked
    val conditional: Expression?, // sig checked
    val operational: Expression?, // sig checked
    val body: Expression,
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.forLoop(this)

    override fun sig(): Signature {
        initializer?.sig()
        conditional?.sig()
        operational?.sig()
        body.sig()

        return Sign.NONE
    }
}