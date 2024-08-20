package space.themelon.eia64.compiler.expressions

import space.themelon.eia64.compiler.Expression
import space.themelon.eia64.compiler.signatures.Sign
import space.themelon.eia64.compiler.signatures.Signature

data class Shadow(
    val names: List<String>,
    val body: Expression
) : Expression() {

    override fun <R> accept(v: Visitor<R>) = v.shado(this)

    override fun sig(): Signature {
        body.sig()
        return Sign.UNIT
    }
}