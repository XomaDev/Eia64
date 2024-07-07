package space.themelon.eia64.analysis

data class VariableType(
    val runtimeType: ExpressionType,
    val primitive: Boolean = true,
    val value: Any? = null
)