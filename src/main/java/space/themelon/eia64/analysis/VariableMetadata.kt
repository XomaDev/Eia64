package space.themelon.eia64.analysis

data class VariableMetadata(
    val runtimeType: ExpressionType,
    private val module: String? = null
) {
    fun getModule(): String? {
        return module ?: when (runtimeType) {
            ExpressionType.INT -> "eint"
            ExpressionType.STRING -> "string"
            ExpressionType.ARRAY -> "array"
            else -> null
        }
    }
}