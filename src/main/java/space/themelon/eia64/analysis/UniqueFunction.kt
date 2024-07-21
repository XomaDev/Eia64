package space.themelon.eia64.analysis

import space.themelon.eia64.signatures.Matching.matches
import space.themelon.eia64.signatures.Signature

data class UniqueFunction(
    val arguments: List<Signature>,
    val reference: FunctionReference
) {
    fun matchesArgs(suppliedArgs: List<Signature>): Boolean {
        val promisedSize = arguments.size
        val suppliedSize = suppliedArgs.size

        if (promisedSize != suppliedSize) return false

        val promisedItr = arguments.iterator()
        val suppliedItr = suppliedArgs.iterator()

        while (promisedItr.hasNext()) {
            val promisedSignature = promisedItr.next()
            val receivedSignature = suppliedItr.next()

            if (!matches(promisedSignature, receivedSignature)) return false
        }
        return true
    }
}