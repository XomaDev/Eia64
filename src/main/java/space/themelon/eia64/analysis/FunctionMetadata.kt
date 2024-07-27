package space.themelon.eia64.analysis

import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token

// Just a simple meta-data holder to pass b/w return calls
// Only used by Parsers
data class FunctionMetadata(
    val where: Token,
    val name: String,
    val args: List<Pair<String, Signature>>,
    val isVoid: Boolean,
    val returnSignature: Signature,
    val reference: FunctionReference
)