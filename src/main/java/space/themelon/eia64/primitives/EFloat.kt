package space.themelon.eia64.primitives

class EFloat(initialValue: Float): Primitive<EFloat>, Comparable<EFloat>, Numeric {

    private var floatValue = initialValue

    override fun set(value: Any) {
        if (value !is EFloat)
            throw RuntimeException("EFloat.set() value is not an EFloat")
        floatValue = value.floatValue
    }

    override fun get() = floatValue

    override fun stdlibName() = "float"

    override fun isCopyable() = true
    override fun copy() = EFloat(floatValue)

    override fun compareTo(other: EFloat) = floatValue.compareTo(other.floatValue)
    override fun toString() = floatValue.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EFloat) return false
        return floatValue == other.floatValue
    }

    override fun hashCode() = floatValue.hashCode()

    fun getAndIncrement() = floatValue++
    fun incrementAndGet() = ++floatValue

    fun getAndDecrement() = floatValue--
    fun decrementAndGet() = --floatValue

    operator fun plus(other: EFloat) = EFloat(floatValue + other.floatValue)
    operator fun plusAssign(other: EFloat) {
        floatValue += other.floatValue
    }

    operator fun minus(other: EFloat) = EFloat(floatValue - other.floatValue)
    operator fun minusAssign(other: EFloat) {
        floatValue -= other.floatValue
    }

    operator fun times(other: EFloat) = EFloat(floatValue * other.floatValue)
    operator fun timesAssign(other: EFloat) {
        floatValue *= other.floatValue
    }

    operator fun div(other: EFloat) = EFloat(floatValue / other.floatValue)
    operator fun divAssign(other: EFloat) {
        floatValue /= other.floatValue
    }

    fun and(other: EFloat) = EFloat(Float.fromBits(floatValue.toRawBits() and other.floatValue.toRawBits()))
    fun or(other: EFloat) = EFloat(Float.fromBits(floatValue.toRawBits() or other.floatValue.toRawBits()))
}