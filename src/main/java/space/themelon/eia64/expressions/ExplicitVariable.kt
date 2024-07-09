package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.VariableMetadata
import space.themelon.eia64.syntax.Token

data class ExplicitVariable(
    val where: Token,
    val typeInfo: VariableMetadata,
    val mutable: Boolean,
    val definition: DefinitionType,
    val expr: Expression
) : Expression(where) {

    init {
        verify()
    }

    private fun verify() {
        val gotSignature = expr.signature()
        if (gotSignature.type != typeInfo.runtimeType) {
            where.error<String>(
                "Variable ${definition.name} expected runtime type ${typeInfo.runtimeType} but got" +
                        " ${gotSignature.type}"
            )
        }
        if (gotSignature.metadata != null
            && gotSignature.metadata.getModule() != typeInfo.getModule()
        ) {
            where.error<String>(
                "Variable ${definition.name} was expected type ${typeInfo.getModule()} " +
                        "but got ${gotSignature.metadata.getModule()}"
            )
        }
    }

    override fun <R> accept(v: Visitor<R>) = v.variable(this)
    override fun signature() = expr.signature()
}