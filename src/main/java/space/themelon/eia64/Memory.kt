package space.themelon.eia64

class Memory(private val memory: Memory? = null) {

    private val values = HashMap<String, Any>()
    private val functions = HashMap<String, Any>()

    var next: Memory? = null

    fun isLower() = memory != null

    fun superMemory(): Memory {
        values.clear()
        // we can save the current instance and reuse it
        memory!!.next = this
        return memory
    }

    fun define(name: String, value: Any) {
        values[name] = value
    }

    fun get(name: String): Any = values[name] ?: memory?.get(name)
    ?: throw RuntimeException("get() unable to find variable $name")

    fun update(name: String, value: Any) {
        if (!values.containsKey(name)) {
            if (memory != null) memory.update(name, value)
            else throw RuntimeException("Unable to find variable $name to update")
        }
        values[name] = value
    }
}