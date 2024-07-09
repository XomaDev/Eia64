package space.themelon.eia64.analysis

data class Signature(
    val holder: String,
    val signature: String,
) {
    override fun equals(other: Any?) = other is Signature && other.signature == signature
    override fun hashCode() = signature.hashCode()

    fun holderCopy(newHolder: String) = Signature(newHolder, signature)
}