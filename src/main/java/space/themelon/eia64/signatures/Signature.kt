package space.themelon.eia64.signatures

abstract class Signature {
    // we cannot self-reference here, or it will end in recursion failure
    var terminative = false
    var returnMetadata: Signature? = null

    // This implies when *this* expression is executed, there will be
    // an end of execution of that scope or function

    fun terminate(signature: Signature) {
        terminative = true
        returnMetadata = signature
    }

    fun copyMetadata(another: Signature): Signature {
        returnMetadata = another.returnMetadata
        terminative = another.terminative
        return this
    }

    fun isInt() = this == Sign.INT
    fun isFloat() = this == Sign.FLOAT

    fun isNumeric() = this == Sign.NUM || this == Sign.INT || this == Sign.FLOAT
    fun isNumericOrChar() = isNumeric() || this == Sign.CHAR

    abstract fun logName(): String
}