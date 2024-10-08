package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.signatures.SimpleSignature
import space.themelon.eia64.syntax.Token

data class Alpha(
    val where: Token,
    val index: Int,
    val value: String,
    val sign: Signature,
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.alpha(this)

    override fun sig(): Signature {
        if (index == -3) {
            // anything have a negative index indicates that there was a function
            // or a static class corresponding to the alpha token
            // but sig() on that token means it's not referring to any of them
            where.error<String>("Cannot find symbol '$value'")
        }
        return sign
    }
}