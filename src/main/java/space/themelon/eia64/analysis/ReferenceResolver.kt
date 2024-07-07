package space.themelon.eia64.analysis

class ReferenceResolver {

    val classes = ArrayList<String>()

    private var currentScope = Scope()

    fun enterScope() {
        val newScope = Scope(currentScope)
        currentScope = newScope
    }

    fun leaveScope(): Boolean {
        // imaginary scope is a scope where you don't have to actually create a new scope
        // you could run without it, consider this situation:
        // let x = 5
        // if (x) { println("Hello, "World") }
        // here you don't require creating a new scope to evaluate it
        val imaginaryScope = currentScope.names.isEmpty() && currentScope.functions.isEmpty()
        currentScope.before.let {
            if (it == null)
                throw RuntimeException("Reached super scope")
            currentScope = it
        }
        return imaginaryScope
    }

    fun defineFn(name: String, fnExpression: FunctionReference) {
        if (name in currentScope.functions)
            throw RuntimeException("Function $name is already defined in the current scope")
        currentScope.functions += name
        currentScope.funcObjs += fnExpression
    }

    fun defineVariable(name: String, type: VariableMetadata) {
        if (name in currentScope.names)
            throw RuntimeException("Variable $name is already defined in the current scope")
        currentScope.names += name
        currentScope.variableMetadata += type
    }

    fun resolveFn(name: String) = currentScope.resolveFn(name, 0)

    fun resolveVr(name: String) = currentScope.resolveVr(name)
}