package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.syntax.Type

data class Shadow(
    val names: List<Pair<String, Type>>,
    val body: Expression
) : Expression() {

    override fun <R> accept(v: Visitor<R>) = v.shado(this)

    override fun sig() = Sign.UNIT
}