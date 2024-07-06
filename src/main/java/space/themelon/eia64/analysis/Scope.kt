package space.themelon.eia64.analysis

import space.themelon.eia64.Expression

class Scope(val before: Scope? = null) {
    val names = ArrayList<String>()
    val functions = ArrayList<String>()

    val variableExprs = ArrayList<ExprType>()
    val funcObjs = ArrayList<FunctionReference>()

    fun resolveFn(name: String, travelDepth: Int): FunctionReference? {
        functions.indexOf(name).let {
            if (it != -1) return funcObjs[it]
            if (before != null) return before.resolveFn(name, travelDepth + 1)
            return null
        }
    }

    fun resolveVr(name: String): VariableReference? {
        names.indexOf(name).let { if (it != -1) return VariableReference(it, variableExprs[it]) }
        if (before != null) return before.resolveVr(name)
        return null
    }
}