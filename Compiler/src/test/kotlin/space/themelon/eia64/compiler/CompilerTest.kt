package space.themelon.eia64.compiler

import java.io.File

object CompilerTest {

    private val PLAYGROUND = File(System.getProperty("user.dir"), "playground")

    @JvmStatic
    fun main(args: Array<String>) {
        val source = File(PLAYGROUND, "hello.eia").readText()
        val compiler = Compiler(File(PLAYGROUND, "hello.eia.e"))

        compiler.fromString(source)
    }
}