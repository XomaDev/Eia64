package space.themelon.eia64.signatures

class ArrayExtension(
    val elementSignature: Signature
): Signature() {
    override fun equals(other: Any?): Boolean {
        return other is ArrayExtension && other.elementSignature == elementSignature
    }

    override fun hashCode(): Int {
        return elementSignature.hashCode()
    }

    override fun toString() = "ArrayExtension<$elementSignature>"
}