package space.themelon.eia64.runtime

enum class Interrupt {
    RETURN,
    BREAK,
    CONTINUE,
    NONE,
}

data class FlowBlack(val interrupt: Interrupt, val data: Any? = null)