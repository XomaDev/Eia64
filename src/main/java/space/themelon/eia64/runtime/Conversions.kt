package space.themelon.eia64.runtime

import space.themelon.eia64.containers.*

object Conversions {

    fun Any.eiaToJava(): Any? {
        if (this is Nothing) return null
        if (this !is Primitive<*>) return this
        if (this is ENil) return null
        return get()
    }

    fun Any?.javaToEia(): Primitive<*> {
        return when (this) {
            is Int -> EInt(this)
            is Float -> EFloat(this)
            is String -> EString(this)
            is Boolean -> EBool(this)
            is Char -> EChar(this)
            null -> ENil()
            else -> EJava(this, "${this::class.java.name}<>")
        }
    }
}