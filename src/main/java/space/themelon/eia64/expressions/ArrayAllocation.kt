package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.ArraySignature
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token

class ArrayAllocation(
    val where: Token,
    val signature: Signature,
    val size: Expression,
): Expression(where) {

    init {
        if (size.sig() != Sign.INT) {
            where.error<String>("Array allocation expects an Int for array length, but got $size")
        }
    }

    override fun <R> accept(v: Visitor<R>): R {
        return v.arrayAllocation(this)
    }

    override fun sig(): Signature {
        return ArraySignature(signature)
    }
}