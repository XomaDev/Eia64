package space.themelon.eia64.analysis

class NameResolver {

    class Scope(private val depth: Int, val before: Scope? = null) {
        val names = ArrayList<String>()
        val functions = ArrayList<String>()

        fun resolveFn(name: String, travelDepth: Int): Pair<Int, Int> {
            functions.indexOf(name).let { if (it != -1) return Pair(depth, it) }
            if (before != null) return before.resolveFn(name, travelDepth + 1)
            throw RuntimeException("Unable to resolve name '$name'")
        }

        fun resolveVr(name: String): Int {
            names.indexOf(name).let { if (it != -1) return it }
            if (before != null) return before.resolveVr(name)
            throw RuntimeException("Unable to resolve variable name '$name'")
        }
    }

    val classes = ArrayList<String>()

    private var depth = 0
    private var currentScope = Scope(depth++)

    fun enterScope() {
        val newScope = Scope(depth++, currentScope)
        currentScope = newScope
    }

    fun leaveScope() {
        currentScope.before.let {
            if (it == null)
                throw RuntimeException("Reached super scope")
            currentScope = it
        }
        depth--
    }

    fun defineFn(name: String) {
        currentScope.functions += name
    }

    fun defineVr(name: String) {
        currentScope.names += name
    }

    fun resolveFn(name: String) = currentScope.resolveFn(name, 0)
    fun resolveVr(name: String): Int {
        val index = currentScope.resolveVr(name)
        println("Resolved $name: $index")
        return index
    }
}