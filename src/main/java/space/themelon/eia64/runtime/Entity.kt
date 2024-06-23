package space.themelon.eia64.runtime

import space.themelon.eia64.Expression
import space.themelon.eia64.syntax.Type
import space.themelon.eia64.syntax.Type.*

data class Entity(
    val name: String,
    val mutable: Boolean,
    var value: Any,
    val type: Type) {

    fun update(another: Any) {
        if (!mutable) throw RuntimeException("Entity $name is immutable")
        if (type == C_ANY) value = another
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
            is Int -> C_INT
            is String -> C_STRING
            is Boolean -> C_BOOL
            is Char -> C_CHAR
            is Expression -> C_UNIT
            is Array<*> -> C_ARRAY
            else -> throw RuntimeException("Unknown type of value $value")
        }
    }
}