package space.themelon.eia64.analysis

data class VariableType(
    val runtimeType: ExprType,
    val primitive: Boolean = true,
    val value: Any? = null
)