package space.themelon.eia64.analysis

class NameScope(val before: NameScope? = null) {
    val names = ArrayList<String>()
    val functions = ArrayList<String>()

    val variableSigns = ArrayList<String>()
    val funcObjs = ArrayList<FunctionReference>()

    fun resolveFn(name: String, travelDepth: Int): FunctionReference? {
        functions.indexOf(name).let {
            if (it != -1) return funcObjs[it]
            if (before != null) return before.resolveFn(name, travelDepth + 1)
            return null
        }
    }

    fun resolveVr(name: String): VariableReference? {
        names.indexOf(name).let { if (it != -1) return VariableReference(it, variableSigns[it]) }
        if (before != null) return before.resolveVr(name)
        return null
    }
}