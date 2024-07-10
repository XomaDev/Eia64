package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.SimpleSignature
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token

data class Cast(
    val where: Token,
    val expr: Expression,
    val signatureCast: Signature
) : Expression(where) {

    // we do not require evaluating this node at runtime
    override fun <R> accept(v: Visitor<R>) = expr.accept(v)

    override fun sig(): Signature {
        // this ensures casting is only done from type Any to <T>
        val exprSign = expr.sig()
        if (exprSign == Sign.ANY) return signatureCast
        where.error<String>("Cannot cast $expr to $signatureCast")
        throw RuntimeException()
    }
}