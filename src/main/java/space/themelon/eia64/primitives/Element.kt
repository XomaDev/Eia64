package space.themelon.eia64.primitives

interface Element {
    fun set(value: Any)
    fun get(): Any
    fun stdlibName(): String
}