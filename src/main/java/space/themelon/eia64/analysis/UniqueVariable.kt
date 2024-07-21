package space.themelon.eia64.analysis

import space.themelon.eia64.signatures.Signature

data class UniqueVariable(
    val index: Int, // nth variable in scope
    val signature: Signature
)