package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.ArrayExtension
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token

data class ArrayLiteral(
    val where: Token,
    val elements: List<Expression>
) : Expression(where) {

    init {
        sig()
    }

    override fun <R> accept(v: Visitor<R>) = v.array(this)

    override fun sig(): Signature {
        // We need to dynamically decide the signature of elements.
        // If not all of them have the same signature, we use ANY Signature
        var signature: Signature

        if (elements.isEmpty()) {
            signature = Sign.ANY
        } else {
            signature = elements[0].sig()
            for (element in elements) {
                val elementSignature = element.sig()
                if (elementSignature != signature) {
                    signature = Sign.ANY
                    // if all the elements don't hold the same signature, use ANY
                    break
                }
            }
        }

        // We need to also store elements signature for array access
        return ArrayExtension(signature)
    }

    fun elementSignature(): Signature {
        // dynamic deciding of array element signature based on content
        if (elements.isEmpty()) return Sign.ANY

        var signature = elements[0].sig()
        for (element in elements) {
            val elementSignature = element.sig()
            if (elementSignature != signature) {
                signature = Sign.ANY
                // if all the elements don't hold the same signature, use ANY
                break
            }
        }
        return signature
    }
}