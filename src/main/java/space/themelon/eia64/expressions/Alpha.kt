package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.Signature
import space.themelon.eia64.syntax.Token

data class Alpha(
    val where: Token,
    val index: Int,
    val value: String,
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.alpha(this)

    override fun sig() = SIGN

    companion object {
        private val SIGN = Signature("Alpha", Sign.ANY)
    }
}