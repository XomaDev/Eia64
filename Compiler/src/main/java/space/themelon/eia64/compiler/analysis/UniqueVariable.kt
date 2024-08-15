package space.themelon.eia64.compiler.analysis

import space.themelon.eia64.compiler.signatures.Signature

data class UniqueVariable(
    val index: Int, // nth variable in scope
    val mutable: Boolean,
    val signature: Signature,
    val public: Boolean
)