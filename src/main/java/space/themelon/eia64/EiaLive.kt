package space.themelon.eia64

import space.themelon.eia64.analysis.ParserX
import space.themelon.eia64.io.FilteredInput
import space.themelon.eia64.io.FilteredOutput
import space.themelon.eia64.runtime.Executor
import space.themelon.eia64.syntax.Lexer
import java.io.PrintStream
import java.util.concurrent.atomic.AtomicReference

class EiaLive(input: FilteredInput, output: FilteredOutput) {

    companion object {
        private val DELETE_CODE = "\b \b".encodeToByteArray()
        //private val OUTPUT_STYLE = "$CYAN$BOLD".encodeToByteArray()
        //val SHELL_STYLE = "$RESET$BLUE_BG eia \$ $RESET ".toByteArray()
        val SHELL_STYLE = "eia \$".toByteArray()
    }

    private fun serve(input: FilteredInput, output: FilteredOutput) {
        val executor = AtomicReference(Executor())

        fun writeShell() {
            output.write(SHELL_STYLE)
            output.write("\r\n".encodeToByteArray())
        }
        writeShell()

        executor.get().apply {
            standardInput = input.input
            standardOutput = PrintStream(output)
        }

        // TODO: implement Array Pattern -> Left -> Right
        // Or vice versa when certain conditions are met Right -> Left
        val codeByts = CodeByteArray()

        fun getFilteredCode() = String(codeByts.get())
            .replace(Regex("\\p{Cntrl}"), "")
            .trim()

        fun execute() {
            val filteredCode = getFilteredCode()
            if (filteredCode.isEmpty()) return
            codeByts.reset()
            //output.write(OUTPUT_STYLE)
            //safeRun(output) {
                executor.get().loadMainSource(filteredCode)
            //}
            writeShell()
        }

        fun lex() {
            val filteredCode = getFilteredCode()
            if (filteredCode.isEmpty()) return
            codeByts.reset()
            //output.write(OUTPUT_STYLE)
            output.write(10)

            //safeRun(output) {
                Lexer(filteredCode).tokens.forEach {
                    output.write(it.toString().encodeToByteArray())
                    output.write(10)
                }
            //}

            writeShell()
        }

        fun parse() {
            println(String(codeByts.get()))
            val filteredCode = getFilteredCode()
            if (filteredCode.isEmpty()) return
            codeByts.reset()
            //output.write(OUTPUT_STYLE)
            output.write(10)

            //safeRun(output) {
                val trees = ParserX(Executor()).parse( Lexer(filteredCode).tokens)

                trees.expressions.forEach {
                    output.write(it.toString().encodeToByteArray())
                    output.write(10)
                }
            //}

            writeShell()
        }

        while (true) {
            val letterCode = input.read()
            if (letterCode == -1) break

            when (letterCode.toChar()) {
                // Delete Char
                '\u007F' -> {
                    if (codeByts.isNotEmpty()) {
                        output.write(DELETE_CODE)
                        codeByts.delete()
                    }
                }

                // Ctrl C
                '\u0003' -> break

                // Ctrl E
                '\u0005' -> execute()

                '\u001B' -> {
                    // Ignore Escape Characters
                    input.read()
                    input.read()
                }

                else -> {
                    output.write(letterCode)
                    codeByts.put(letterCode.toByte())
                }
            }
        }
    }
}