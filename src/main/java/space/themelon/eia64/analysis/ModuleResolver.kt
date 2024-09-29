package space.themelon.eia64.analysis

import space.themelon.eia64.runtime.Entity
import space.themelon.eia64.syntax.Token

abstract class ModuleResolver {
    abstract fun resolveGlobalVrImaginary(name: String): Entity
    abstract fun resolveGlobalVr(where: Token, name: String): UniqueVariable?
    abstract fun resolveGlobalFn(where: Token, name: String, numArgs: Int): FunctionReference?
}
