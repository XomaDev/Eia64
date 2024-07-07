package space.themelon.eia64.analysis

data class VariableMetadata(
    val runtimeType: ExpressionType,
    val primitive: Boolean = true,
    val value: Any? = null
) {
    fun copy() = VariableMetadata(runtimeType, primitive, value)
}