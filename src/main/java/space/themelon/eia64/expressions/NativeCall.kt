package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.Matching.matches
import space.themelon.eia64.signatures.SignatureConstants
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type

data class FunctionInfo(
    val signature: Signature?, // return type of functions
    val argsSize: Int,
    val argSignatures: List<Pair<String, Signature>> = emptyList()
)

data class NativeCall(
    val where: Token,
    val call: Type,
    val arguments: List<Expression>, // sig checked
) : Expression() {

    companion object {
        private val OLD_FUNCTION_SIGNATURES = HashMap<Type, FunctionInfo>().apply {
            put(Type.PRINT, FunctionInfo(SignatureConstants.NONE, -1))
            put(Type.PRINTLN, FunctionInfo(SignatureConstants.NONE, -1))
            put(Type.LEN, FunctionInfo(SignatureConstants.INT, 1, listOf("measurable" to SignatureConstants.ANY)))
            put(Type.SLEEP, FunctionInfo(SignatureConstants.NONE, 1, listOf("millis" to SignatureConstants.INT)))
            put(Type.RAND, FunctionInfo(SignatureConstants.INT, 2, listOf("from" to SignatureConstants.INT, "to" to SignatureConstants.INT)))
            put(Type.INT_CAST, FunctionInfo(SignatureConstants.INT, 1, listOf("intCastable" to SignatureConstants.ANY)))
            put(Type.EXIT, FunctionInfo(SignatureConstants.NONE, 1, listOf("exitCode" to SignatureConstants.INT)))

            put(Type.FLOAT_CAST, FunctionInfo(SignatureConstants.FLOAT, 1, listOf("floatCastable" to SignatureConstants.ANY)))
            put(Type.CHAR_CAST, FunctionInfo(SignatureConstants.CHAR, 1, listOf("charCastable" to SignatureConstants.ANY)))
            put(Type.BOOL_CAST, FunctionInfo(SignatureConstants.BOOL, 1, listOf("boolCastable" to SignatureConstants.ANY)))
            put(Type.STRING_CAST, FunctionInfo(SignatureConstants.STRING, 1, listOf("stringCastable" to SignatureConstants.ANY)))

            put(Type.TIME, FunctionInfo(SignatureConstants.INT, 0))
            put(Type.READ, FunctionInfo(SignatureConstants.STRING, 0))
            put(Type.READLN, FunctionInfo(SignatureConstants.STRING, 0))
            put(Type.FORMAT, FunctionInfo(SignatureConstants.STRING, -1))
            put(Type.TYPE_OF, FunctionInfo(SignatureConstants.TYPE, 1, listOf("any" to SignatureConstants.ANY)))

            put(Type.MEM_CLEAR, FunctionInfo(SignatureConstants.NONE, 0))
            put(Type.COPY, FunctionInfo(null, 1, listOf("any" to SignatureConstants.ANY)))
        }
    }

    override fun <R> accept(v: Visitor<R>) = v.nativeCall(this)

    override fun sig(): Signature {
        arguments.forEach { it.sig() } // functions like println() have indefinite args
        val functionInfo = OLD_FUNCTION_SIGNATURES[call] ?: where.error("Could not find native function type $call")
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
            val argInfo = expectedSignatureIterator.next()
            val argName = argInfo.first
            val expectedSignature = argInfo.second

            val gotSignature = argumentIterator.next().sig()

            if (!matches(expectedSignature, gotSignature)) {
                where.error<String>("Function $callName() expected arg signature $expectedSignature for $argName but got $gotSignature")
            }
        }

        return returnSignature
    }
}