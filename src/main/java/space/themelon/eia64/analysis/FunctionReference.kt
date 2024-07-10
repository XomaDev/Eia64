package space.themelon.eia64.analysis

import space.themelon.eia64.expressions.Function
import space.themelon.eia64.signatures.Signature

data class FunctionReference(
    var fnExpression: Function? = null,
    val signs: List<Pair<String, Signature>>, // List < < ParameterName, Signature >
    val argsSize: Int,
    val returnSignature: Signature
) {
    // it is important to override toString() or else it may cause recursive StackOverFlow error
    override fun toString() = "<${fnExpression?.name ?: "UnsetFunctionReference"}()>"
}