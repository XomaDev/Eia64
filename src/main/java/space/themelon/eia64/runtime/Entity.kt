package space.themelon.eia64.runtime

import space.themelon.eia64.Expression
import space.themelon.eia64.primitives.EBool
import space.themelon.eia64.primitives.EChar
import space.themelon.eia64.primitives.EInt
import space.themelon.eia64.primitives.EString
import space.themelon.eia64.syntax.Type
import space.themelon.eia64.syntax.Type.*

open class Entity(
    private val name: String,
    private val mutable: Boolean,
    var value: Any,
    val type: Type) {

    open fun update(another: Any) {
        if (!mutable) throw RuntimeException("Entity $name is immutable")
        if (type == E_ANY) value = another
        else {
            val otherType = getType(another)
            if (otherType != type) throw RuntimeException("Entity $name cannot change type $type to $otherType")
            value = unbox(another)
        }
    }

    companion object {
        fun unbox(value: Any): Any {
            return if (value is Entity) {
                // break that return boxing
                if (value.type == RETURN) unbox(value.value)
                else value.value
            } else value
        }

        fun getType(value: Any): Type = when (value) {
            is Entity -> {
                // break that return unboxing
                if (value.type == RETURN) getType(value.value)
                else value.type
            }
            is EInt -> E_INT
            is EString -> E_STRING
            is EBool -> E_BOOL
            is EChar -> E_CHAR
            is Expression -> E_UNIT
            is Array<*> -> E_ARRAY
            else -> throw RuntimeException("Unknown type of value $value")
        }
    }
}