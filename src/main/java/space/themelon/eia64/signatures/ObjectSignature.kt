package space.themelon.eia64.signatures

class ObjectSignature(
    val extensionClass: String // could be `Object` or a `Car` (Object extension) or a `Bus`
) : Signature() {
    override fun equals(other: Any?): Boolean {
        if (other is ObjectSignature && extensionClass == Sign.OBJECT.type) {
            // <Object Extension> can be assigned to <Object>
            // e.g. let vehicle: Object = new Car()
            // but not let vehicle: Car = new Bus()
            return true
        }
        return other is SimpleSignature && other.type == Sign.ANY.type
    }

    override fun hashCode() = extensionClass.hashCode()

    override fun toString() = "ObjectExtension<$extensionClass>"
}