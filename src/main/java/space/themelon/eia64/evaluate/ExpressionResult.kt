package space.themelon.eia64.evaluate

data class ExpressionResult(val result: Result, val value: Any? = null)

enum class Result {
    RETURN,
    BREAK,
    CONTINUE,
    OK,
    NONE
}