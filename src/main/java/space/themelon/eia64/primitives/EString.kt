package space.themelon.eia64.primitives

class EString(
    initialValue: String
): ArrayOperable<Char>, Element {

    private var string = initialValue

    override fun getAt(index: Int) = string[index]

    override fun getAt(index: Int, value: Char) {
        string = string.replaceRange(index, index, string)
    }

    override fun set(value: Any) {
        string = when (value) {
            is String -> value
            !is EString -> throw IllegalArgumentException("EString.set() value is not a String")
            else -> value.string
        }
    }

    override fun get(): String = string

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EString) return false
        return string == other.string
    }

    override fun hashCode() = string.hashCode()
}