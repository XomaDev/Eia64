package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.*
import space.themelon.eia64.signatures.Matching.matches
import space.themelon.eia64.syntax.Token

class ArrayAllocation(
    val where: Token,
    private val elementSignature: Signature,
    @Consumable("Unexpected void expression for array size") val size: Expression,
    @Consumable("Cannot use a void expression as a default value for array") val defaultValue: Expression,
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