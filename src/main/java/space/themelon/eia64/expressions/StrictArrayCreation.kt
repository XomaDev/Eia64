package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.ArrayExtension
import space.themelon.eia64.signatures.Matching.matches
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token

class StrictArrayCreation(
    val where: Token,
    val elementSignature: Signature,
    val elements: List<Expression>,
) : Expression() {

    override fun <R> accept(v: Visitor<R>) = v.arrayCreation(this)

    override fun sig(): Signature {
        elements.forEach {
            val expressionSignature = it.sig()
            if (!matches(elementSignature, it.sig())) {
                where.error<String>(
                    "Array has signature of $elementSignature " +
                            "but contains element of signature $expressionSignature"
                )
            }
        }

        // Self-signature is an Array, but elements are of $elementSignature.
        // We need to store element signatures.
        // Just like an Object extension
        return ArrayExtension(elementSignature)
    }
}