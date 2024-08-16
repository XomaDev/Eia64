package space.themelon.eia64.compiler

import space.themelon.eia64.compiler.bytecode.BytecodeVisitor
import space.themelon.eia64.compiler.bytecode.Dumper
import space.themelon.eia64.compiler.syntax.Lexer
import java.io.File
import java.io.FileOutputStream

class Compiler(
    outputFile: File,
) {

    companion object {
        const val DEBUG = true
        const val STD_LIB = "" // TODO
        const val EXECUTION_DIRECTORY = "" // TODO
    }

    private val dumper = Dumper(FileOutputStream(outputFile))

    private val externalExecutors = HashMap<String, BytecodeVisitor>()
    private val mainEvaluator = BytecodeVisitor(dumper)

    private val externalParsers = HashMap<String, Parser>()
    private val mainParser = Parser(this)

    // for now it is a one time use
    fun fromString(source: String) {
        mainEvaluator.dump(mainParser.parse(Lexer(source).tokens))
        dumper.close()
    }

    // Dont touch them for now
    // Let's deal with them later
    fun addModule(sourceFile: String, name: String): Boolean {
        if (externalParsers[name] != null) return false
        externalParsers[name] = Parser(this).also { it.parse(getTokens(sourceFile)) }
        return true
    }

    private fun getTokens(sourceFile: String) = Lexer(File(sourceFile).readText()).tokens

    fun getModule(name: String) = externalParsers[name] ?: throw RuntimeException("Could not find module '$name'")
}