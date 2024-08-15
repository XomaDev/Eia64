package space.themelon.eia64.compiler

import space.themelon.eia64.compiler.syntax.Token

data class ModuleInfo(
    val where: Token, // for debugging and throwing errors
    val name: String,
    // linked indicates if the module is associated with
    // a specific data type, such as " a string ", number 123, or an array
    val linked: Boolean
)