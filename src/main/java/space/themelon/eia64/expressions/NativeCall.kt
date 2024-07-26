package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.Matching.matches
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type

data class FunctionInfo(
    val signature: Signature?, // return type of functions
    val argsSize: Int,
    val argSignatures: List<Signature> = emptyList()
)

data class NativeCall(
    val where: Token,
    val call: Type,
    val arguments: List<Expression>,
) : Expression(where) {

    companion object {
        private val FUNCTION_SIGNATURES = HashMap<Type, FunctionInfo>().apply {
            put(Type.PRINT, FunctionInfo(Sign.INT, -1))
            put(Type.PRINTLN, FunctionInfo(Sign.INT, -1))
            put(Type.LEN, FunctionInfo(Sign.INT, 1, listOf(Sign.ANY)))
            put(Type.SLEEP, FunctionInfo(Sign.INT, 1, listOf(Sign.INT)))
            put(Type.RAND, FunctionInfo(Sign.INT, 1, listOf(Sign.INT)))
            put(Type.INT_CAST, FunctionInfo(Sign.INT, 1, listOf(Sign.ANY)))
            put(Type.EXIT, FunctionInfo(Sign.INT, 1, listOf(Sign.INT)))

            put(Type.FLOAT_CAST, FunctionInfo(Sign.FLOAT, 1, listOf(Sign.ANY)))
            put(Type.CHAR_CAST, FunctionInfo(Sign.CHAR, 1, listOf(Sign.ANY)))
            put(Type.BOOL_CAST, FunctionInfo(Sign.BOOL, 1, listOf(Sign.ANY)))
            put(Type.STRING_CAST, FunctionInfo(Sign.STRING, 1, listOf(Sign.ANY)))

            put(Type.READ, FunctionInfo(Sign.STRING, 0))
            put(Type.READLN, FunctionInfo(Sign.STRING, 0))
            put(Type.FORMAT, FunctionInfo(Sign.STRING, -1))
            put(Type.TYPE_OF, FunctionInfo(Sign.TYPE, 1, listOf(Sign.ANY)))

            put(Type.INCLUDE, FunctionInfo(Sign.BOOL, 1, listOf(Sign.STRING)))
            put(Type.MEM_CLEAR, FunctionInfo(Sign.BOOL, 0))
            put(Type.COPY, FunctionInfo(null, 1, listOf(Sign.ANY)))
        }
    }

    init {
        sig()
    }

    override fun <R> accept(v: Visitor<R>) = v.nativeCall(this)

    override fun sig(): Signature {
        val functionInfo = FUNCTION_SIGNATURES[call] ?: where.error("Could not find native function type $call")
        val expectedArgsSize = functionInfo.argsSize
        val gotArgsSize = arguments.size
        val callName = call.name.lowercase()

        if (expectedArgsSize != -1 && gotArgsSize != expectedArgsSize) {
            where.error<String>("Function $callName() expected $expectedArgsSize args but got $gotArgsSize")
        }
        val returnSignature = functionInfo.signature ?: return arguments[0].sig()
        val expectedSignatureIterator = functionInfo.argSignatures.iterator()
        val argumentIterator = arguments.iterator()

        while (expectedSignatureIterator.hasNext()) {
            val expectedSignature = expectedSignatureIterator.next()
            val gotSignature = argumentIterator.next().sig()

            if (!matches(expectedSignature, gotSignature)) {
                // TODO:
                //  in future this error should be more descriptive, should tell which exact
                //  argument was mismatched
                where.error<String>("Function $callName() expected arg signature $expectedSignature but got $gotSignature")
            }
        }

        return returnSignature
    }
}