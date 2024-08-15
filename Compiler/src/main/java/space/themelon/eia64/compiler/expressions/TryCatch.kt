package space.themelon.eia64.compiler.expressions

import space.themelon.eia64.compiler.Expression
import space.themelon.eia64.compiler.signatures.Sign
import space.themelon.eia64.compiler.signatures.Signature
import space.themelon.eia64.compiler.syntax.Token

data class TryCatch(
    val where: Token,
    val tryBlock: Expression, // sig checked
    val catchIdentifier: String,
    val catchBlock: Expression, // sig checked
): Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.tryCatch(this)

    override fun sig(): Signature {
        val trySignature = tryBlock.sig()
        val catchSignature = catchBlock.sig()

        if (trySignature == catchSignature) return trySignature
        return Sign.ANY
    }
}