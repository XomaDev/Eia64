package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.FunctionReference
import space.themelon.eia64.signatures.Consumable
import space.themelon.eia64.signatures.Matching.matches
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token
import kotlin.math.exp

data class MethodCall(
    val name: Token,
    val reference: FunctionReference,
    @Consumable("Arguments cannot contain void expressions") val arguments: List<Expression>,
) : Expression(name) {

    init {
        sig()
    }

    override fun <R> accept(v: Visitor<R>) = v.methodCall(this)

    override fun sig(): Signature {
        val argSigns = reference.signs

        val expectedArgsSize = argSigns.size
        val suppliedArgsSize = arguments.size

        if (expectedArgsSize != suppliedArgsSize) {
            name.error<String>("Function $name expected $expectedArgsSize arguments but got $suppliedArgsSize")
        }

        val signIterator = argSigns.iterator()
        val argIterator = arguments.iterator()

        while (signIterator.hasNext()) {
            val argInfo = signIterator.next() // <ParameterName, Sign>

            val argName = argInfo.first
            val expectedArgSign = argInfo.second
            val suppliedArgSign = argIterator.next().sig()

            if (!matches(expectedArgSign, suppliedArgSign)) {
                name.error<String>("Function $name expected $expectedArgSign for argument $argName but got $suppliedArgSign")
            }
        }
        return reference.returnSignature
    }
}