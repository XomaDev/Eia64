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
    var mature: Boolean
) : Expression(where) {

    init {
        // happens to early defined functions
        //if (!mature) {
            //throw RuntimeException("Not mature $value")
        //}
    }

    override fun <R> accept(v: Visitor<R>) = v.alpha(this)

    // Verify -> child
    override fun sig(): Signature {
        if (!mature) {
            where.error<Expression>("Could not resolve name $value")
        }
        return sign
    }
}