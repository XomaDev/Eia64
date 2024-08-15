package space.themelon.eia64.compiler.expressions

import space.themelon.eia64.compiler.Expression
import space.themelon.eia64.compiler.signatures.Sign

data class Include(
    val names: List<String>
) : Expression(null) {

    override fun <R> accept(v: Visitor<R>): R = v.include(this)

    override fun sig() = Sign.NONE
}