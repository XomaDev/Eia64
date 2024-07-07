package space.themelon.eia64.analysis

data class ExpressionSignature(
    val type: ExpressionType,
    val metadata: VariableMetadata? = null
)