package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.ArrayExtension
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token

class StrictArrayCreation(
    val where: Token,
    private val elementSignature: Signature,
    val elements: List<Expression>,
): Expression() {

    init {
        elements.forEach {
            val expressionSignature = it.sig()
            if (it.sig() != elementSignature) {
                where.error<String>("Array has signature of $elementSignature " +
                        "but contains element of signature $expressionSignature")
            }
        }
    }

    override fun <R> accept(v: Visitor<R>): R {
        return v.arrayCreation(this)
    }

    override fun sig(): Signature {
        // Self-signature is an Array, but elements are of $elementSignature.
        // We need to store element signatures.
        // Just like an Object extension
        return ArrayExtension(elementSignature)
    }
}