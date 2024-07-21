package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.FunctionReference
import space.themelon.eia64.analysis.ModuleInfo
import space.themelon.eia64.signatures.Matching.matches
import space.themelon.eia64.signatures.Consumable
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token

data class ClassMethodCall(
    val where: Token,
    val static: Boolean,
    val obj: Expression,
    val method: String,
    @Consumable("Arguments cannot contain void expressions") val arguments: List<Expression>,
    val reference: FunctionReference,
    val moduleInfo: ModuleInfo,
) : Expression(where) {

    init {
        sig()
    }

    override fun <R> accept(v: Visitor<R>) = v.classMethodCall(this)

    override fun sig(): Signature {
        val declarationSigns = reference.signs

        val expectedArgsSize = declarationSigns.size
        var suppliedArgsSize = arguments.size

        if (moduleInfo.linked) {
            // Consider this case:
            //  println(" Meow ".trim())
            // By evaluator, it would be translated to
            //  println(String.trim(" Meow "))
            suppliedArgsSize++
        }

        if (expectedArgsSize != suppliedArgsSize) {
            where.error<String>("Function $method in module [${moduleInfo.name}] expected $expectedArgsSize arguments but got $suppliedArgsSize")
        }

        val signIterator = declarationSigns.iterator()
        val argIterator = arguments.iterator()

        if (moduleInfo.linked) {
            val selfSignature = signIterator.next().second
            val providedSignature = obj.sig()
            if (!matches(selfSignature, providedSignature)) {
                where.error<String>("Self argument mismatch, expected $selfSignature, got $providedSignature")
            }
        }
        while (signIterator.hasNext()) {
            val argInfo = signIterator.next() // <ParameterName, Sign>

            val argName = argInfo.first
            val expectedArgSign = argInfo.second
            val suppliedArgSign = argIterator.next().sig()

            if (!matches(expectedArgSign, suppliedArgSign)) {
                where.error<String>("Function $method in module [${moduleInfo.name}] expected $expectedArgSign for argument $argName but got $suppliedArgSign")
            }
        }
        return reference.returnSignature
    }
}