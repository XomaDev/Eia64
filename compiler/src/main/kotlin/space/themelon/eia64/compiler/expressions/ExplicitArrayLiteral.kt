package space.themelon.eia64.compiler.expressions

import space.themelon.eia64.compiler.Expression
import space.themelon.eia64.compiler.signatures.ArrayExtension
import space.themelon.eia64.compiler.signatures.Matching.matches
import space.themelon.eia64.compiler.signatures.Signature
import space.themelon.eia64.compiler.syntax.Token

class ExplicitArrayLiteral(
    val where: Token,
    val elementSignature: Signature,
    val elements: List<Expression>,
) : Expression() {

    override fun <R> accept(v: Visitor<R>) = v.explicitArrayLiteral(this)

    override fun sig(): Signature {
        for ((index, expression) in elements.withIndex()) {
            val expressionSignature = expression.sig()
            if (!matches(elementSignature, expression.sig())) {
                where.error<String>(
                    "Array has signature of $elementSignature " +
                            "but contains element of signature $expressionSignature at index $index"
                )
            }
        }

        return ArrayExtension(elementSignature)
    }

    fun elementSignature() = elementSignature
}