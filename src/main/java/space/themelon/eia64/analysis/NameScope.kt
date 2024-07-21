package space.themelon.eia64.analysis

import space.themelon.eia64.signatures.Signature

class NameScope(val before: NameScope? = null) {
    val names = ArrayList<String>()
    val functions = HashMap<String, UniqueFunction>()

    val variableSigns = ArrayList<Signature>()

    fun resolveFn(name: String, args: List<Signature>): FunctionReference? {
        val uniqueFunction = functions[name]
        if (uniqueFunction != null && uniqueFunction.matchesArgs(args)) {
            return uniqueFunction.reference
        }
        if (before != null) return before.resolveFn(name, args)
        return null
    }

    // just checks if a function with that name exists in scope
    fun resolveFnName(name: String): Boolean {
        if (functions.containsKey(name)) return true
        if (before != null) return before.resolveFnName(name)
        return false
    }

    fun resolveVr(name: String): VariableReference? {
        names.indexOf(name).let { if (it != -1) return VariableReference(it, variableSigns[it]) }
        if (before != null) return before.resolveVr(name)
        return null
    }
}