package space.themelon.eia64

import space.themelon.eia64.runtime.Environment
import java.io.File

object Eia {

    private val directory = File(System.getProperty("user.dir"))

    @JvmStatic
    fun main(args: Array<String>) {
        val iterator = args.iterator()
        val live: Boolean // true => live mode, else a file
        var sourceFile = ""

        if (args.isEmpty()) {
            // defaults to live mode
            live = true
        } else {
            val type = iterator.next()
            if (type == "live") live = true
            else {
                live = false
                sourceFile = type
            }
        }
        val props = HashMap<String, String>(3)
        while (iterator.hasNext()) {
            iterator.next().split("=").let {
                if (it.size == 2) props[it[0]] = it[1]
            }
        }
        props["debug"]?.let { Environment.DEBUG = it == "true" }

        val environment = Environment()
        if (!sourceFile.startsWith('/')) {
            sourceFile = directory.absolutePath + "/" + sourceFile
        }
        val file = File(sourceFile)
        if (!file.isFile || !file.exists()) {
            println("Cannot find source file '$file', make sure it is a full valid path")
            return
        }
        environment.parse(file.readText())
    }
}