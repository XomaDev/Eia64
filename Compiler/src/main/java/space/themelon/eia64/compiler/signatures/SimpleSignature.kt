package space.themelon.eia64.compiler.signatures

class SimpleSignature(val type: String) : Signature() {
    override fun equals(other: Any?) = other is SimpleSignature && other.type == type
    override fun hashCode() = type.hashCode()

    override fun logName() = type

    override fun toString() = "<$type>"
}