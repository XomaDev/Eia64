package space.themelon.eia64.compiler

import java.io.File

object CompilerTest {

    private val PLAYGROUND = File(System.getProperty("user.dir"), "playground")

    @JvmStatic
    fun main(args: Array<String>) {
        val source = File(PLAYGROUND, "hello.eia").readText()
        val compiled = File(PLAYGROUND, "hello.eia.e")
        val compiler = Compiler(compiled)

        compiler.fromString(source)

        val disassembled = File(PLAYGROUND, "hello.eia.asm")
        Disassembler(compiled, disassembled)
    }
}