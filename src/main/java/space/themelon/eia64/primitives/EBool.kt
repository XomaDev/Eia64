package space.themelon.eia64.primitives

class EBool(initialValue: Boolean): Element {

    private var boolValue = initialValue

    override fun set(value: Any) {
        if (value !is EBool)
            throw RuntimeException("EBool.set() value is not a Bool")
        boolValue = value.boolValue
    }

    override fun get() = boolValue

    fun and(other: EBool) = EBool(boolValue && other.boolValue)
    fun or(other: EBool) = EBool(boolValue || other.boolValue)


    override fun toString() = boolValue.toString()
}