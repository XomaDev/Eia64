package space.themelon.eia64.analysis

import space.themelon.eia64.expressions.Function

data class FunctionReference(
    var fnExpression: Function? = null,
    val signs: List<Pair<String, String>>, // List < < ParameterName, Sign >
    val returnSign: String
) {
    // it is important to override toString() or else it may cause recursive StackOverFlow error
    override fun toString() = "<${fnExpression?.name ?: "UnsetFunctionReference"}()>"
}