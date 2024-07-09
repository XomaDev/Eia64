package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.ExpressionSignature
import space.themelon.eia64.analysis.ExpressionType
import space.themelon.eia64.analysis.FunctionReference
import space.themelon.eia64.analysis.VariableMetadata
import space.themelon.eia64.syntax.Token

data class MethodCall(
    val name: Token,
    val functionReference: FunctionReference,
    val arguments: List<Expression>,
) : Expression(name) {

    init {
        verify()
    }

    private fun verify() {
        val argSignature = functionReference.arguments
        val expectedArgs = argSignature.size
        val gotArgs = arguments.size

        if (expectedArgs != gotArgs) {
            name.error<String>("Function $name() expected $expectedArgs args but got $gotArgs args")
            return
        }

        val argumentItr = arguments.iterator()
        val signatureItr = argSignature.iterator()

        while (argumentItr.hasNext()) {
            // TODO:
            //  in future, we need to also check the Object type (there could be multiple object types)

            val expected = signatureItr.next()
            val argName = expected.name

            val gotSignature = argumentItr.next().signature()

            val expectedMetadata = expected.metadata
            val gotMetadata = gotSignature.metadata

            if (expectedMetadata.runtimeType != gotMetadata!!.runtimeType) {
                name.error<String>("Arg '$argName' in function $name() expected type ${expectedMetadata.runtimeType} but got $gotSignature")
                return
            }

            if (expectedMetadata.getModule() != gotMetadata.getModule()) {
                name.error<String>(
                    "Arg '$argName' in function $name() expected type" +
                            " '${expectedMetadata.getModule()}' but got '${gotMetadata.getModule()}'"
                )
                return
            }
        }
    }

    override fun <R> accept(v: Visitor<R>) = v.methodCall(this)

    override fun signature(): ExpressionSignature {
        val returnType = ExpressionType.translate(functionReference.returnType)
        return ExpressionSignature(returnType, VariableMetadata(returnType))
    }
}