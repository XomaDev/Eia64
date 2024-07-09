package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.Signature
import space.themelon.eia64.syntax.Token

data class ForEach(
    val where: Token,
    val name: String,
    val entity: Expression,
    val body: Expression,
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.forEach(this)

    override fun sig() = Signature("ForEach", Sign.ANY)
}