package space.themelon.eia64.analysis

import space.themelon.eia64.signatures.Signature

class ResolutionScope(val before: ResolutionScope? = null) {
    // these hooks are dispatched just before the scope ends
    val scopeHooks = mutableListOf<() -> Unit>()
    val uniqueFunctionNames = LinkedHashSet<String>()

    // val functionOutlines = HashMap<UniqueFunction, FunctionReference>
    val functions = HashMap<UniqueFunction, FunctionReference>()
    val variables = HashMap<String, UniqueVariable>()

    fun dispatchHooks() {
        scopeHooks.forEach { it() }
    }

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

    fun defineVr(name: String,
                 mutable: Boolean,
                 signature: Signature,
                 public: Boolean) {
        variables[name] = UniqueVariable(variables.size, mutable, signature, public)
    }

    fun resolveVr(name: String): UniqueVariable? {
        val uniqueVariable = variables[name]
        if (uniqueVariable != null) return uniqueVariable
        if (before != null) return before.resolveVr(name)
        return null
    }
}