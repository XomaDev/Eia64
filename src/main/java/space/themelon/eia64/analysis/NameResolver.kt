package space.themelon.eia64.analysis

class NameResolver {

    class Scope(val before: Scope?) {
        val names = ArrayList<String>()
        val functions = ArrayList<String>()

        fun resolveFn(name: String): Int {
            functions.indexOf(name).let { if (it != -1) return it }
            if (before != null) return before.resolveFn(name)
            throw RuntimeException("Unable to resolve name '$name'")
        }

        fun resolveVr(name: String): Int {
            names.indexOf(name).let { if (it != -1) return it }
            if (before != null) return before.resolveVr(name)
            throw RuntimeException("Unable to resolve name '$name'")
        }
    }

    private var currentScope = Scope(null)

    fun enterScope() {
        val newScope = Scope(currentScope)
        currentScope = newScope
    }

    fun leaveScope() {
        currentScope.before.let {
            if (it == null)
                throw RuntimeException("Reached super scope")
            currentScope = it
        }
    }

    fun defineFn(name: String) {
        currentScope.functions += name
    }

    fun defineVr(name: String) {
        currentScope.names += name
    }

    fun resolveFn(name: String): Int = currentScope.resolveFn(name)
    fun resolveVr(name: String): Int = currentScope.resolveVr(name)
}