package space.themelon.eia64.expressions

import space.themelon.eia64.syntax.Type

data class DefinitionType(
    val name: String,
    val type: Type,
    val className: String? = null
)