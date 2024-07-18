package space.themelon.eia64.signatures

abstract class Signature {
    // we cannot self-reference here, or it will end in recursion failure
    var terminative = false
    var returnMetadata: Signature? = null

    // This implies when *this* expression is executed, there will be
    // an end of execution of that scope or function

    fun terminate(signature: Signature) {
        terminative = true
        returnMetadata = signature
    }

    fun copyMetadata(another: Signature): Signature {
        returnMetadata = another.returnMetadata
        terminative = another.terminative
        return this
    }
}