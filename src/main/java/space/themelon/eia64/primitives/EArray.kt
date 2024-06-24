package space.themelon.eia64.primitives

class EArray(initialValue: Array<Any>): Element, ArrayOperable<Any> {

    private var arrayValue = initialValue

    val size: Int
        get() = arrayValue.size

    override fun set(value: Any) {
        if (!(value is Array<*> && value.isArrayOf<Any>()))
            throw RuntimeException("EArray.set() value is not an Array")
        @Suppress("UNCHECKED_CAST")
        arrayValue = value as Array<Any>
    }

    override fun get() = arrayValue

    override fun getAt(index: Int): Any = arrayValue[index]

    override fun setAt(index: Int, value: Any) {
        arrayValue[index] = value
    }

    override fun stdlibName() = "array"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as EArray
        return arrayValue.contentEquals(other.arrayValue)
    }

    override fun hashCode() = arrayValue.contentHashCode()
    override fun toString() = "EArray(${arrayValue.contentToString()})"
}