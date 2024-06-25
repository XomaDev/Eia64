package space.themelon.eia64.primitives

interface Element<T> {
    fun set(value: Any)
    fun get(): Any
    fun stdlibName(): String
    fun copy(): T
}