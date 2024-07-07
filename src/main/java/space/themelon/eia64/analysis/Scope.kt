package space.themelon.eia64.analysis

class Scope(val before: Scope? = null) {
    val names = ArrayList<String>()
    val functions = ArrayList<String>()

    val variableMetadata = ArrayList<VariableMetadata>()
    val funcObjs = ArrayList<FunctionReference>()

    fun resolveFn(name: String, travelDepth: Int): FunctionReference? {
        functions.indexOf(name).let {
            if (it != -1) return funcObjs[it]
            if (before != null) return before.resolveFn(name, travelDepth + 1)
            return null
        }
    }

    fun resolveVr(name: String): VariableReference? {
        names.indexOf(name).let { if (it != -1) return VariableReference(it, variableMetadata[it]) }
        if (before != null) return before.resolveVr(name)
        return null
    }
}