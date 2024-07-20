package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.*
import space.themelon.eia64.signatures.Matching.matches
import space.themelon.eia64.syntax.Token

data class Cast(
    val where: Token,
    @Consumable("Cannot cast from void expression") val expr: Expression,
    val expectSignature: Signature
) : Expression(where) {

    // we actually do require evaluating this node at runtime
    override fun <R> accept(v: Visitor<R>) = v.cast(this)

    override fun sig(): Signature {
        val exprSign = expr.sig()
        // Allow casting from Any to <T>
        if (exprSign == Sign.ANY) return expectSignature
        if (expectSignature is ObjectExtension) {
            // Cast attempt from Object<Object> to Object<X>
            //  any other attempts fail
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
        } else if (expectSignature is ArrayExtension) {
            // Cast attempt from Array<Any> to Array<Y>
            if (exprSign !is ArrayExtension) {
                where.error<String>("Cannot cast $expr into array type $exprSign")
                throw RuntimeException()
            }
            val castArrayType = expectSignature.elementSignature
            val currentArrayType = exprSign.elementSignature
            if (!matches(currentArrayType,  castArrayType)) {
                where.error<String>("Cannot cast array element type $currentArrayType into $castArrayType")
            }
            return expectSignature
        }
        if (exprSign == expectSignature) {
            // they already are of the same type
            return expectSignature
        }
        where.error<String>("Cannot cast $expr to $expectSignature")
        throw RuntimeException()
    }
}