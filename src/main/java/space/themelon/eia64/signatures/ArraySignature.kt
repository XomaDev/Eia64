package space.themelon.eia64.signatures

class ArraySignature(
    val elementSignature: Signature
): Signature() {
    override fun equals(other: Any?): Boolean {
        if (other is ArraySignature && elementSignature == other.elementSignature) {
            // An array extension signature, it specifies the element types in an array
            // let places = arrayOf("India", "Japan", "Russia")
            // let places = arrayOf<String>("India", "Japan", "Russia")
            // let places = ["India", "Japan", "Russia"]
            return true
        }
        return other is SimpleSignature && other.type == Sign.ANY.type
    }

    override fun hashCode(): Int {
        return elementSignature.hashCode()
    }

    override fun toString() = "ArrayExtension<$elementSignature>"
}