package space.themelon.eia64

import space.themelon.eia64.TerminalColors.BLUE_BG
import space.themelon.eia64.TerminalColors.BOLD
import space.themelon.eia64.TerminalColors.CYAN
import space.themelon.eia64.TerminalColors.RESET
import space.themelon.eia64.analysis.ParserX
import space.themelon.eia64.runtime.Executor
import space.themelon.eia64.syntax.Lexer
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.util.concurrent.atomic.AtomicReference

class EiaLive(
    private val input: InputStream,
    private val output: OutputStream
) {

    companion object {
        private val DELETE_CODE = "\b \b".encodeToByteArray()

        private val OUTPUT_STYLE = "$CYAN$BOLD".encodeToByteArray()
        val SHELL_STYLE = "$RESET$BLUE_BG eia \$ $RESET ".toByteArray()
    }

    init {
        serve()
    }

    private fun serve() {
        val executor = AtomicReference(Executor())

        fun writeShell() {
            output.write(SHELL_STYLE)
            output.write("\r\n".encodeToByteArray())
        }
        writeShell()

        executor.get().apply {
            standardInput = input
            standardOutput = PrintStream(output)
        }

        val sourceCode = StringBuilder()
        fun getSourceCode(): String? = if (sourceCode.isEmpty()) null else {
            val code = sourceCode.toString()
            sourceCode.setLength(0)
            code
        }

        fun execute() {
            val code = getSourceCode() ?: return
            output.write(OUTPUT_STYLE)
            runSafely(output) {
                executor.get().loadMainSource(code)
            }
            writeShell()
        }

        fun lex() {
            val code = getSourceCode() ?: return
            output.write(OUTPUT_STYLE)
            output.write(10)

            runSafely(output) {
                Lexer(code).tokens.forEach {
                    output.write(it.toString().encodeToByteArray())
                    output.write(10)
                }
            }

            writeShell()
        }

        fun parse() {
            val code = getSourceCode() ?: return
            output.write(OUTPUT_STYLE)
            output.write(10)

            runSafely(output) {
                val nodes = ParserX(Executor()).parse(Lexer(code).tokens)

                nodes.expressions.forEach {
                    output.write(it.toString().encodeToByteArray())
                    output.write(10)
                }
            }

            writeShell()
        }

        while (true) {
            sourceCode.append(input.read())
        }
    }

    private fun runSafely(
        output: OutputStream,
        block: () -> Unit
    ) {
        try {
            block()
        } catch (e: Exception) {
            output.write("${e.message.toString()}\n".encodeToByteArray())
        }
    }
}