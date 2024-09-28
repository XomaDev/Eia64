package space.themelon.eia64.analysis

import space.themelon.eia64.syntax.Token

abstract class ModuleResolver {
    abstract fun resolveGlobalVr(where: Token, name: String): UniqueVariable?
    abstract fun resolveGlobalFn(where: Token, name: String, numArgs: Int): FunctionReference?
}
