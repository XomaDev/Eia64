package space.themelon.eia64.containers

class EDouble(initialValue: Double): Primitive<EDouble>, Comparable<EDouble>, Numeric {

    private var doubleValue = initialValue

    override fun set(value: Any) {
        if (value !is EDouble)
            throw RuntimeException("EDouble.set() value is not EDouble")
        doubleValue = value.doubleValue
    }

    override fun get() = doubleValue
    override fun isCopyable() = true
    override fun copy() = EDouble(doubleValue)

    override fun compareTo(number: Numeric) = doubleValue.compareTo(number.get().toDouble())
    override fun compareTo(other: EDouble) = doubleValue.compareTo(other.doubleValue)

    override fun getAndIncrement() = doubleValue++
    override fun incrementAndGet() = ++doubleValue

    override fun getAndDecrement() = doubleValue--
    override fun decrementAndGet() = --doubleValue

    override operator fun plus(number: Numeric) = EDouble(doubleValue + number.get().toDouble())
    override operator fun plusAssign(number: Numeric) {
        doubleValue += number.get().toDouble()
    }

    override operator fun minus(number: Numeric) = EDouble(doubleValue - number.get().toDouble())
    override operator fun minusAssign(number: Numeric) {
        doubleValue -= number.get().toDouble()
    }

    override operator fun times(number: Numeric) = EDouble(doubleValue * number.get().toDouble())
    override operator fun timesAssign(number: Numeric) {
        doubleValue *= number.get().toDouble()
    }

    override operator fun div(number: Numeric) = EDouble(doubleValue / number.get().toDouble())
    override operator fun divAssign(number: Numeric) {
        doubleValue /= number.get().toDouble()
    }

    override fun and(number: Numeric) =
        EDouble(Double.fromBits(doubleValue.toRawBits() and number.get().toDouble().toRawBits()))
    override fun or(number: Numeric) =
        EDouble(Double.fromBits(doubleValue.toRawBits() or number.get().toDouble().toRawBits()))

    override fun toString() = doubleValue.toString()

    override fun equals(other: Any?): Boolean {
        if (other !is Numeric) return false
        return doubleValue == other.get().toDouble()
    }

    override fun hashCode() = doubleValue.hashCode()
}