package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.FunctionReference
import space.themelon.eia64.signatures.ObjectSignature
import space.themelon.eia64.signatures.SimpleSignature
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token

data class NewObj(
    val where: Token,
    val name: String,
    val arguments: List<Expression>,
    val reference: FunctionReference // of init() function
) : Expression(where) {

    init {
        sig()
    }

    override fun <R> accept(v: Visitor<R>) = v.new(this)

    override fun sig(): Signature {
        val argSigns = reference.signs

        val expectedArgsSize = argSigns.size
        val suppliedArgsSize = arguments.size

        if (expectedArgsSize != suppliedArgsSize) {
            where.error<String>("init() expected $expectedArgsSize arguments but got $suppliedArgsSize")
        }

        val signIterator = argSigns.iterator()
        val argIterator = arguments.iterator()

        while (signIterator.hasNext()) {
            val argInfo = signIterator.next() // <ParameterName, Sign>

            val argName = argInfo.first
            val expectedArgSign = argInfo.second
            val suppliedArgSign = argIterator.next().sig()

            if (expectedArgSign != Sign.ANY && expectedArgSign != suppliedArgSign) {
                where.error<String>("init() expected $expectedArgSign for argument $argName but got $suppliedArgSign")
            }
        }
        return ObjectSignature(name)
    }
}