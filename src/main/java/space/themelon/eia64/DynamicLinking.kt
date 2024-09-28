package space.themelon.eia64

import space.themelon.eia64.analysis.FunctionReference
import space.themelon.eia64.analysis.ModuleResolver
import space.themelon.eia64.analysis.UniqueVariable
import space.themelon.eia64.expressions.DynamicLinkBody
import space.themelon.eia64.expressions.FunctionExpr
import space.themelon.eia64.runtime.Executor
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.SimpleSignature
import space.themelon.eia64.syntax.Token
import java.lang.reflect.Method

object DynamicLinking {
    // Helps to dynamically add classes.
    @Suppress("UNCHECKED_CAST")
    fun addStatic(
        executor: Executor,
        moduleName: String,
        // variable resolve callback
        callback: Any,
        vrResolveCallbackMethod: Method,
        // function resolve callback,
        fnResolveCallbackMethod: Method,
        // function invoke callback,
        fnInvokeCallbackMethod: Method,
    ) {
        executor.addImaginaryModule(moduleName, object : ModuleResolver() {
            override fun resolveGlobalVr(
                where: Token,
                name: String
            ): UniqueVariable {
                val resolved = vrResolveCallbackMethod.invoke(callback, moduleName, name)
                    ?: throw RuntimeException("Unable to resolve dynamic class variable $name in module $moduleName")
                resolved as Array<*>
                val index = resolved[0] as Int
                val mutable = resolved[1] as Boolean
                val signature =
                    (resolved[2] as String).let { Sign.MAPPING[it] ?: throw RuntimeException("Unknown signature $it") }
                val public = resolved[3] as Boolean
                return UniqueVariable(index, mutable, signature, public)
            }

            override fun resolveGlobalFn(
                where: Token,
                name: String,
                numArgs: Int
            ): FunctionReference {
                val resolved = fnResolveCallbackMethod.invoke(callback, moduleName, name)
                    ?: throw RuntimeException("Unable to resolve dynamic function $name in module $moduleName")
                resolved as Array<*>
                // Array<Array<String>>  <ParameterName, Signature>
                // noinspection UNCHECKED_CAST
                val parameters = resolved[0] as Array<*>
                val parsedParameters = ArrayList<Pair<String, SimpleSignature>>()
                parameters.forEach {
                    it as Array<*>
                    parsedParameters += it[0] as String to
                            it[1].let { sig -> Sign.MAPPING[sig] ?: throw RuntimeException("Unknown signature $sig") }
                }
                val argsSize = parameters.size
                val returnSignature = (resolved[1] as String).let { Sign.MAPPING[it] ?: throw RuntimeException("Unknown signature $it") }
                val isVoid = resolved[2] as Boolean

                val fnExpr = FunctionExpr(
                    where,
                    name,
                    parsedParameters,
                    isVoid,
                    returnSignature,
                    DynamicLinkBody(returnSignature) { args ->
                        return@DynamicLinkBody fnInvokeCallbackMethod.invoke(callback, moduleName, name, args as Any)
                    }
                )
                return FunctionReference(
                    where,
                    name,
                    fnExpr,
                    parsedParameters,
                    argsSize,
                    returnSignature,
                    isVoid,
                    public = true,
                    -1
                )
            }
        })
    }
}
