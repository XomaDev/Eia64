package space.themelon.eia64

import space.themelon.eia64.runtime.Entity
import space.themelon.eia64.syntax.Type

class Memory(private val memory: Memory? = null) {

    private val values = HashMap<String, Any>()

    var next: Memory? = null

    fun isLower() = memory != null

    fun superMemory(): Memory {
        values.clear()
        // we can save the current instance and reuse it
        memory!!.next = this
        return memory
    }

    fun defineFunc(name: String, value: Any) {
        if (search(name) != null)
            throw RuntimeException("Entity $name is already defined")
        values[name] = value
    }
    
    fun defineVar(name: String, value: Any, mutable: Boolean, type: Type) {
        values[name] = Entity(name, mutable, value, type)
    }
    
    fun get(name: String): Any = search(name) ?: throw RuntimeException("get() unable to find entity $name")

    private fun search(name: String): Any? = values[name] ?: memory?.search(name)
}