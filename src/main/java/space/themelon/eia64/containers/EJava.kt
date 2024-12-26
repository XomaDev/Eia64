package space.themelon.eia64.containers
class EJava(
    initialValue: Any,
    val name: String,
): Primitive<EJava> {

    private var value = initialValue

    override fun set(value: Any) {
        if (value !is EJava)
            throw RuntimeException("EJava.set() is not an EJava")
        this.value = value
    }

    override fun get() = value

    override fun isCopyable() = false

    override fun copy() = throw UnsupportedOperationException()

    override fun toString() = value.toString()
}