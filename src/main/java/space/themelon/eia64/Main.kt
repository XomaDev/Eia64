package space.themelon.eia64

import java.io.File

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val loader = javaClass.classLoader
        val syntaxFile = File(loader.getResource("syntax")!!.file)
        val source = loader.getResource("hello.eia")!!.readText()

        SyntaxAnalysis(syntaxFile).tokenize(source)

    }
}