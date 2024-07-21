package space.themelon.eia64.analysis

import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature

class ScopeManager {

    val classes = ArrayList<String>()
    val staticClasses = ArrayList<String>()

    // Helps us to know if `continue` and `break` statements
    // are allowed in the current scope
    // 0 => Not Allowed
    // > 0 => Allowed
    private var iterativeScopes = 0
    val isIterativeScope
        get() = iterativeScopes > 0

    fun <T> iterativeScope(block: () -> T): T {
        iterativeScopes++
        val t = block()
        iterativeScopes--
        return t
    }

    private var expectedReturnSignature: Signature = Sign.NONE
    val getPromisedSignature
        get() = expectedReturnSignature

    fun <T> expectReturn(signature: Signature, block: () -> T): T {
        val parentSignature = expectedReturnSignature
        expectedReturnSignature = signature
        val t = block()
        expectedReturnSignature = parentSignature
        return t
    }

    private var currentScope = NameScope()

    fun enterScope() {
        val newScope = NameScope(currentScope)
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

    fun defineFn(name: String, args: List<Signature>, reference: FunctionReference) {
        if (name in currentScope.functions)
            throw RuntimeException("Function $name is already defined in the current scope")
        currentScope.functions[name] = UniqueFunction(args, reference)
    }

    fun defineVariable(name: String, signature: Signature) {
        if (name in currentScope.names)
            throw RuntimeException("Variable $name is already defined in the current scope")
        currentScope.names += name
        currentScope.variableSigns += signature
    }

    fun resolveFnName(name: String) = currentScope.resolveFnName(name)
    fun resolveFn(name: String, suppliedArgs: List<Signature>) = currentScope.resolveFn(name, suppliedArgs)

    fun resolveVr(name: String) = currentScope.resolveVr(name)
}