package space.themelon.eia64.primitives

class EChar(initialValue: Char): Element {

    private var charValue = initialValue

    override fun set(value: Any) {
        charValue = when (value) {
            is Char -> charValue
            !is EChar -> throw IllegalArgumentException("EChar.set() value is not a Char")
            else -> value.charValue
        }
    }

    override fun get() = charValue

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EChar) return false
        return charValue == other.charValue
    }

    override fun hashCode() = charValue.hashCode()
}