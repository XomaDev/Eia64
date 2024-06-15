package space.themelon.eia64.syntax

data class Token(val types: Array<Type>, val symbol: String? = null) {

    val firstType: Type get() = types[0]
    fun hasType(type: Type): Boolean = types.contains(type)

    override fun toString(): String {
        return if (symbol != null) {
            "($symbol ${types.contentToString()})"
        } else return types.contentToString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Token

        if (!types.contentEquals(other.types)) return false
        if (symbol != other.symbol) return false

        return true
    }

    override fun hashCode(): Int {
        var result = types.contentHashCode()
        result = 31 * result + symbol.hashCode()
        return result
    }
}