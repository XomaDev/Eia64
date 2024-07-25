package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.Matching.verifyNonVoids
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type

data class NativeCall(
    val where: Token,
    val call: Type,
    val arguments: List<Expression>,
) : Expression(where) {

    init {
        verifyNonVoids(arguments, where, "Cannot accept a void expression")
    }

    override fun <R> accept(v: Visitor<R>) = v.nativeCall(this)

    override fun sig() = when (call) {
        Type.PRINT,
        Type.PRINTLN,
        Type.SLEEP,
        Type.LEN,
        Type.INT_CAST,
        Type.TIME,
        Type.RAND,
        Type.EXIT -> Sign.INT

        Type.FLOAT_CAST -> Sign.FLOAT
        Type.CHAR_CAST -> Sign.CHAR

        Type.READ,
        Type.READLN,
        Type.FORMAT,
        Type.STRING_CAST,
        Type.TYPE -> Sign.STRING

        Type.BOOL_CAST, Type.INCLUDE, Type.MEM_CLEAR -> Sign.BOOL
        Type.COPY -> {
            if (arguments.size != 1) where.error<String>("copy() expects least one argument")
            arguments[0].sig()
        }
        else -> where.error("Unknown native call type $call")
    }
}