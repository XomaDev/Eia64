package space.themelon.eia64

data class Token(val types: List<String>, val symbol: String) {
    val firstType: String get() = types[0]
    fun hasType(type: String): Boolean = type.contains(type)

    override fun toString() = "($symbol $types)"
}