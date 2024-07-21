package space.themelon.eia64.analysis

import space.themelon.eia64.signatures.Signature

class ResolutionScope(val before: ResolutionScope? = null) {
    val uniqueFunctionNames = LinkedHashSet<String>()

    val functions = HashMap<UniqueFunction, FunctionReference>()
    val variables = HashMap<String, UniqueVariable>()

    fun resolveFn(function: UniqueFunction): FunctionReference? {
        val reference = functions[function]
        if (reference != null) return reference
        if (before != null) return before.resolveFn(function)
        return null
    }

    fun resolveFnName(name: String): Boolean {
        if (uniqueFunctionNames.contains(name)) return true
        if (before != null) return before.resolveFnName(name)
        return false
    }

    fun defineVr(name: String, signature: Signature) {
        variables[name] = UniqueVariable(variables.size, signature)
    }

    fun resolveVr(name: String): UniqueVariable? {
        val uniqueVariable = variables[name]
        if (uniqueVariable != null) return uniqueVariable
        if (before != null) return before.resolveVr(name)
        return null
    }
}