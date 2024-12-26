package space.themelon.eia64.containers

interface ArrayOperable<T> {
    fun getAt(index: Int): T
    fun setAt(index: Int, value: T)
}