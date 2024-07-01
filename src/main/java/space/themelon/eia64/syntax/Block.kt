package space.themelon.eia64.syntax

data class Block(
    val line: Int,
    var next: Block? = null,
    var completed: Boolean = false
) {
    val list: ArrayList<Token> = ArrayList()
    var index = 0

    fun fullyEmptied() = index == list.size && next == null

    fun emptied() = index == list.size
    fun peekToken() = list[index]
    fun nextToken() = list[index++]
}