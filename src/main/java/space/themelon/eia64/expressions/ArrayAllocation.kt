package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.ArrayExtension
import space.themelon.eia64.signatures.Matching
import space.themelon.eia64.signatures.Matching.matches
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type

class ArrayAllocation(
    val where: Token,
    private val elementSignature: Signature,
    val size: Expression,
    val defaultValue: Expression,
) : Expression(where) {

    init {
        if (!matches(Sign.INT, size.sig())) {
            where.error<String>("Array allocation expects an Int for array length, but got $size")
        }
        if (!matches(elementSignature, defaultValue.sig())) {
            where.error<String>("arralloc() element signature and default value does not match (type mismatch)")
        }
    }

    override fun <R> accept(v: Visitor<R>): R {
        return v.arrayAllocation(this)
    }

    override fun sig(): Signature {
        return ArrayExtension(elementSignature)
    }
}