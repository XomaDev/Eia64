package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.FunctionReference
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.signatures.SimpleSignature
import space.themelon.eia64.syntax.Token

data class ClassMethodCall(
    val where: Token,
    val static: Boolean,
    val objectInvocation: Boolean,
    val obj: Expression,
    val method: String,
    val arguments: List<Expression>,
    val reference: FunctionReference,
    val module: String
) : Expression(where) {

    init {
        sig()
    }

    override fun <R> accept(v: Visitor<R>) = v.classMethodCall(this)

    override fun sig(): Signature {
        val argSigns = reference.signs

        val expectedArgsSize = argSigns.size
        var suppliedArgsSize = arguments.size

        if (objectInvocation) {
            //
            suppliedArgsSize--
        }

        if (expectedArgsSize != suppliedArgsSize) {
            where.error<String>("Function $method in module [$module] expected $expectedArgsSize arguments but got $suppliedArgsSize")
        }

        val signIterator = argSigns.iterator()
        val argIterator = arguments.iterator()

        while (signIterator.hasNext()) {
            val argInfo = signIterator.next() // <ParameterName, Sign>

            val argName = argInfo.first
            val expectedArgSign = argInfo.second
            val suppliedArgSign = argIterator.next().sig()

            if (expectedArgSign != Sign.ANY && expectedArgSign != suppliedArgSign) {
                where.error<String>("Function $method in module [$module] expected $expectedArgSign for argument $argName but got $suppliedArgSign")
            }
        }
        return reference.returnSignature
    }
}