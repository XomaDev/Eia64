package space.themelon.eia64.primitives

class EInt(initialValue: Int): Element {

    private var intValue = initialValue

    override fun set(value: Any) {
        intValue = when (value) {
            is Int -> value
            !is EInt -> throw RuntimeException("EInt.set() value is not an Int")
            else -> value.intValue
        }
    }

    override fun get() = intValue

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EInt) return false
        return intValue == other.intValue
    }

    override fun hashCode() = intValue
}