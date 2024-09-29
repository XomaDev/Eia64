package space.themelon.eia64.primitives

class EJava(value: Any): Primitive<Any> {

    private var objValue: Any = value

    override fun set(value: Any) {
        if (value !is EJava) {
            throw RuntimeException("EJava.set() value is not EJava")
        }
    }

    override fun get() = objValue

    override fun stdlibName(): String {
        throw UnsupportedOperationException("No stdlib for EJava yet")
    }

    override fun isCopyable() = false

    override fun copy(): Any {
        throw UnsupportedOperationException("Copy on EJava not possible")
    }

    override fun javaValue() = objValue

    override fun toString() = "EJava($objValue)"
}