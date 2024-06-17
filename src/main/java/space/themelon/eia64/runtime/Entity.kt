package space.themelon.eia64.runtime

import space.themelon.eia64.syntax.Type
import space.themelon.eia64.syntax.Type.*

data class Entity(val name: String, val mutable: Boolean, var value: Any, val type: Type) {
    fun update(another: Any) {
        if (!mutable)
            throw RuntimeException("Variable $name is already immutable")
        if (type == Type.C_ANY) value = another
        else {
            val otherType = getType(another)
            if (otherType != type)
                throw RuntimeException("Entity $name cannot change type $type to $otherType")
            value = unbox(another)
        }
    }

    companion object {
        fun unbox(value: Any) = if (value is Entity) value.value else value

        fun getType(value: Any) = when (value) {
            is Entity -> value.type
            is Int -> C_INT
            is String -> C_STRING
            is Boolean -> C_BOOL
            is Char -> C_CHAR
            else -> throw RuntimeException("Unknown type of value $value")
        }
    }
}