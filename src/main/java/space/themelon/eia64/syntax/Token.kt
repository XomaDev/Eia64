package space.themelon.eia64.syntax

import kotlin.jvm.Throws

data class Token(
    val lineCount: Int,
    val type: Type,
    val flags: Array<Flag> = arrayOf(),
    val data: Any? = null
) {

    fun hasFlag(type: Flag): Boolean = flags.contains(type)

    @Throws(RuntimeException::class)
    fun <T> error(message: String): T {
        throw RuntimeException("[line $lineCount] [$this] $message")
    }

    override fun toString(): String {
        val flagsString = flags.contentToString()
        return if (data == null) "($type, $flagsString)" else "($type, $flagsString, od=$data)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Token

        if (type != other.type) return false
        if (!flags.contentEquals(other.flags)) return false
        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + flags.contentHashCode()
        result = 31 * result + (data?.hashCode() ?: 0)
        return result
    }

}