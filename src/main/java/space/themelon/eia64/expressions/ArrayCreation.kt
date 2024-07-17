package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token

class ArrayCreation(
    val where: Token,
    val signature: Signature,
    val elements: List<Expression>,
): Expression() {

    init {
        elements.forEach {
            val elementSignature = it.sig()
            if (it.sig() != signature) {
                where.error<String>("Array has signature of $signature " +
                        "but contains element of signature $elementSignature")
            }
        }
    }

    override fun <R> accept(v: Visitor<R>): R {
        return v.arrayCreation(this)
    }

    override fun sig(): Signature {
        return Sign.ARRAY
    }
}