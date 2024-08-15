package space.themelon.eia64.compiler

import space.themelon.eia64.compiler.analysis.Parser
import space.themelon.eia64.compiler.syntax.Lexer
import java.io.File

class Compiler {

    companion object {
        const val DEBUG = true
        const val STD_LIB = "" // TODO
        const val EXECUTION_DIRECTORY = "" // TODO
    }


    private val externalParsers = HashMap<String, Parser>()
    private val mainParser = Parser(this)

    fun withString(source: String) {
        // TODO, lex it, parse it, and dump it
    }

    fun addModule(sourceFile: String, name: String): Boolean {
        if (externalParsers[name] != null) return false
        externalParsers[name] = Parser(this).also { it.parse(getTokens(sourceFile)) }
        return true
    }

    private fun getTokens(sourceFile: String) = Lexer(File(sourceFile).readText()).tokens

    fun getModule(name: String) = externalParsers[name] ?: throw RuntimeException("Could not find module '$name'")
}