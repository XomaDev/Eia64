package space.themelon.eia64.analysis

class NameResolver {
    class Scope(val before: Scope? = null) {
        val names = ArrayList<String>()
        val functions = ArrayList<String>()

        val funcObjs = HashMap<String, FnElement>()

        fun resolveFn(name: String, travelDepth: Int): FnElement? {
            functions.indexOf(name).let {
                if (it != -1) return funcObjs[name]!!
                if (before != null) return before.resolveFn(name, travelDepth + 1)
                return null
            }
        }

        fun resolveVr(name: String): Int {
            names.indexOf(name).let { if (it != -1) return it }
            if (before != null) return before.resolveVr(name)
            return -1
        }
    }

    val classes = ArrayList<String>()

    private var currentScope = Scope()

    fun enterScope() {
        val newScope = Scope( currentScope)
        currentScope = newScope
    }

    fun leaveScope() {
        currentScope.before.let {
            if (it == null)
                throw RuntimeException("Reached super scope")
            currentScope = it
        }
    }

    fun defineFn(name: String, fnExpression: FnElement) {
        if (name in currentScope.functions)
            throw RuntimeException("Function $name is already defined in the current scope")
        currentScope.functions += name
        currentScope.funcObjs[name] = fnExpression
    }

    fun defineVr(name: String) {
        if (name in currentScope.names)
            throw RuntimeException("Variable $name is already defined in the current scope")
        currentScope.names += name
    }

    fun resolveFn(name: String) = currentScope.resolveFn(name, 0)

    fun resolveVr(name: String): Int {
        val index = currentScope.resolveVr(name)
        return index
    }
}