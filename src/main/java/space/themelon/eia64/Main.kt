package space.themelon.eia64

import space.themelon.eia64.runtime.Environment
import java.io.File

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val directory = File(System.getProperty("user.dir"))
        if (args.isNotEmpty()) {
            val iterator = args.iterator()
            if (args[0] == "debug") {
                Environment.DEBUG = true
                iterator.next()
            }
            startProcess(iterator, directory)
        } else startProcess(args.iterator(), directory) // args is empty
    }

    private fun startProcess(argsIterator: Iterator<String>, directory: File) {
        val next = if (argsIterator.hasNext()) argsIterator.next() else null
        val environment = Environment()
        val startTime = System.nanoTime()

        var sourceFile = next
        if (!sourceFile?.startsWith('/')!!) sourceFile = directory.absolutePath + "/" + sourceFile
        val file = File(sourceFile)
        if (!file.isFile || !file.exists()) {
            println("Cannot find source file '$file', make sure it is a full valid path")
            return
        }
        environment.evaluate(environment.parse(file.readText()))
        println("Took " + (System.nanoTime() - startTime) + " ns")
    }
}