package space.themelon.eia64.syntax

data class Token(
    val type: Type,
    val flags: Array<Type>,
    val optionalData: Any? = null
) {

    fun hasFlag(type: Type): Boolean = flags.contains(type)

    override fun toString(): String {
        val flagsString = flags.contentToString()
        return if (optionalData == null) "($type, $flagsString)" else "($type, $flagsString, od=$optionalData)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Token

        if (type != other.type) return false
        if (!flags.contentEquals(other.flags)) return false
        if (optionalData != other.optionalData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + flags.contentHashCode()
        result = 31 * result + (optionalData?.hashCode() ?: 0)
        return result
    }

}