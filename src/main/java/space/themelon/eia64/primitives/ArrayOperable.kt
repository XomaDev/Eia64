package space.themelon.eia64.primitives

interface ArrayOperable<T> {
    fun getAt(index: Int): T
    fun getAt(index: Int, value: T)
}