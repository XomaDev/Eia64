package space.themelon.eia64.compiler.signatures

import space.themelon.eia64.signatures.Signature

class ArrayExtension(
    val elementSignature: Signature
): Signature() {
    override fun equals(other: Any?) = other is ArrayExtension && other.elementSignature == elementSignature

    override fun hashCode() = elementSignature.hashCode()

    override fun logName() = "Array<$elementSignature>"

    override fun toString() = "ArrayExtension<$elementSignature>"
}