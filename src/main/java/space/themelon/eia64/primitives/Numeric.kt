package space.themelon.eia64.primitives

interface Numeric {
    fun get(): Number

    operator fun plus(number: Numeric): Numeric { throw NotImplementedError() }
    operator fun minus(number: Numeric): Numeric { throw NotImplementedError() }
    operator fun times(number: Numeric): Numeric { throw NotImplementedError() }
    operator fun div(number: Numeric): Numeric { throw NotImplementedError() }

    fun and(number: Numeric): Numeric { throw NotImplementedError() }
    fun or(number: Numeric): Numeric { throw NotImplementedError() }

    operator fun compareTo(number: Numeric): Int { throw NotImplementedError() }
}