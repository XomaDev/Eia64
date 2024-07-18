package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.ObjectExtension
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token

data class Cast(
    val where: Token,
    val expr: Expression,
    val expectSignature: Signature
) : Expression(where) {

    // we actually do require evaluating this node at runtime
    override fun <R> accept(v: Visitor<R>) = v.cast(this)

    override fun sig(): Signature {
        // this ensures casting is only done from type Any to <T>
        val exprSign = expr.sig()
        if (exprSign == Sign.ANY) return expectSignature
        if (expectSignature is ObjectExtension) {
            if (exprSign !is ObjectExtension) {
                where.error<String>("Cannot cast object type $expr to $expectSignature")
                throw RuntimeException()
            }
            val expectClass = expectSignature.extensionClass
            val gotClass = exprSign.extensionClass
            if (gotClass != Sign.OBJECT_SIGN && expectClass != gotClass) {
                where.error<String>("Cannot cast class $gotClass into $expectClass")
            }
            return expectSignature
        }
        where.error<String>("Cannot cast $expr to $expectSignature")
        throw RuntimeException()
    }
}