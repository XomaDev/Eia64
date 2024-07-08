package space.themelon.eia64.analysis

import space.themelon.eia64.syntax.Type

data class ValueDefinition(
    val name: String,
    val type: Type,
    val metadata: VariableMetadata
)