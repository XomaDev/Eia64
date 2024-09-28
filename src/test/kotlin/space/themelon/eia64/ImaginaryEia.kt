package space.themelon.eia64

import space.themelon.eia64.analysis.FunctionReference
import space.themelon.eia64.analysis.ModuleResolver
import space.themelon.eia64.analysis.UniqueVariable
import space.themelon.eia64.runtime.Executor
import space.themelon.eia64.syntax.Token
import java.io.File
import java.lang.reflect.Proxy

object ImaginaryEia {
    @JvmStatic
    fun main(args: Array<String>) {
        val directory = File(System.getProperty("user.dir"))
        val stdlib = File("$directory/stdlib")
        if (!stdlib.isDirectory || !stdlib.exists()) {
            println("Cannot find stdlib/ in the local directory")
            return
        }
        Executor.STD_LIB = stdlib.absolutePath
        val executor = Executor()
        executor.addImaginaryModule("Notifier1", object: ModuleResolver() {
            override fun resolveGlobalFn(where: Token, name: String, numArgs: Int): FunctionReference? {
                if (name == "ShowAlert") {

                }
                return null
            }

            override fun resolveGlobalVr(where: Token, name: String): UniqueVariable? {
                return null
            }
        })
    }
}