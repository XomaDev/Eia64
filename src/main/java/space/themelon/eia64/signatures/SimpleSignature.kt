package space.themelon.eia64.signatures

class SimpleSignature(val type: String) : Signature() {
    override fun equals(other: Any?) = other is SimpleSignature && other.type == type
    override fun hashCode() = type.hashCode()

    override fun toString() = "SimpleSignature<$type>"
}