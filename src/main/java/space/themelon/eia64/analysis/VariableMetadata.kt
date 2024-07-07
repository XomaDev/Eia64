package space.themelon.eia64.analysis

data class VariableMetadata(
    val runtimeType: ExpressionType,
    val module: Any? = null
) {
    fun copy() = VariableMetadata(runtimeType, module)
}