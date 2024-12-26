package space.themelon.eia64.analysis

import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature

class ScopeManager {

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

    private val headScope = ResolutionScope()
    private var currentScope = headScope

    fun enterScope() {
        val newScope = ResolutionScope(currentScope)
        currentScope = newScope
    }

    fun leaveScope(): Boolean {
        // imaginary scope is a scope where you don't have to actually create a new scope
        // you could run without it, consider this situation:
        // let x = 5
        // if (x) { println("Hello, "World") }
        // here you don't require creating a new scope to evaluate it
        // Calls awaiting hooks that must be called before scope ends
        currentScope.dispatchHooks()

        val imaginaryScope = currentScope.variables.isEmpty() && currentScope.functions.isEmpty()
        currentScope.before.let {
            if (it == null)
                throw RuntimeException("Reached super scope")
            currentScope = it
        }
        return imaginaryScope
    }

    // Skeleton of the function that is defined by semi-parser
    fun defineSemiFn(name: String, reference: FunctionReference) {
        //println("Defining $name")
        val unique = UniqueFunction(name, reference.argsSize)
        val existing = currentScope.resolveFn(unique)
        if (existing != null) {
            throw RuntimeException("Function $name is already defined in the current scope")
        }
        currentScope.apply {
            functions[unique] = reference
            sequentialFunctions += reference
            uniqueFunctionNames += name
        }
    }

    fun readFnOutline(): FunctionReference = currentScope.sequentialFunctions.pop()

    // If it is marked Visible, then it can be indexed by external Parsers/Resolvers
    fun defineVariable(name: String, signature: Signature) {
        if (currentScope.resolveVr(name) != null)
            throw RuntimeException("Variable $name is already defined")
        currentScope.defineVr(name, signature)
    }

    fun hasFunctionNamed(name: String) = currentScope.resolveFnName(name)
    fun resolveFn(name: String, numArgs: Int) = currentScope.resolveFn(UniqueFunction(name, numArgs))

    fun resolveVr(name: String) = currentScope.resolveVr(name)
}