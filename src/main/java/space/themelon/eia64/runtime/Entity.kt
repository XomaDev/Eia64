package space.themelon.eia64.runtime

import space.themelon.eia64.Expression
import space.themelon.eia64.primitives.*
import space.themelon.eia64.signatures.ArrayExtension
import space.themelon.eia64.signatures.Matching.matches
import space.themelon.eia64.signatures.ObjectExtension
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature

open class Entity(
    private val name: String,
    private val mutable: Boolean,
    var value: Any,
    val signature: Signature,
    val interruption: InterruptionType = InterruptionType.NONE,
) {

    open fun update(another: Any) {
        if (!mutable) throw RuntimeException("Entity $name is immutable")
        if (signature == Sign.ANY) value = another
        else {
            val otherSignature = getSignature(another)
            if (!matches(signature, otherSignature)) throw RuntimeException("Entity $name cannot change type $signature to $otherSignature")
            value = unbox(another)
        }
    }

    companion object {
        fun unbox(value: Any): Any {
            return if (value is Entity) {
                // break that return boxing
                if (value.interruption != InterruptionType.NONE) unbox(value.value)
                else value.value
            } else value
        }

        fun getSignature(value: Any): Signature = if (value is Entity) {
            // repeatedly break that repeat unboxing
            //  to retrieve the underlying value
            if (value.interruption != InterruptionType.NONE) getSignature(value.value)
            else value.signature
        }
        else if (value is ENil) Sign.NIL
        else if (value is EInt) Sign.INT
        else if (value is EFloat) Sign.FLOAT
        else if (value is EString) Sign.STRING
        else if (value is EBool) Sign.BOOL
        else if (value is EChar) Sign.CHAR
        else if (value is EArray) ArrayExtension(value.elementSignature)
        else if (value is EType) Sign.TYPE
        else if (value is Expression) Sign.UNIT
        else if (value is Evaluator) ObjectExtension(value.className)
        else throw RuntimeException("Unknown type of value $value")
    }
}