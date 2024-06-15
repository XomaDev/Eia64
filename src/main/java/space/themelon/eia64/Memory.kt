package space.themelon.eia64

class Memory(memory: Memory? = null) {

    private val values = HashMap<String, Any>()

    fun define(name: String, value: Any) {
        values[name] = value
    }

    fun get(name: String): Any {
        return values[name] ?: throw RuntimeException("get() unable to find variable $name")
    }
}