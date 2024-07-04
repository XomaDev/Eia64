package space.themelon.eia64.runtime

import space.themelon.eia64.analysis.Parser
import space.themelon.eia64.syntax.Lexer
import java.io.File

class Executor {

    companion object {
        var STD_LIB = "" // will be set
        var EXECUTION_DIRECTORY = File(System.getProperty("user.dir")).absolutePath
    }

    init {
        if (STD_LIB.isBlank())
            throw RuntimeException("STD_LIB is not set")
    }

    private val staticExternalModules = HashMap<String, Evaluator>()
    private val mainEvaluator = Evaluator(this)

    private val externalParsers = HashMap<String, Parser>()
    private val mainParser = Parser(this)

    fun loadMainFile(sourceFile: String) {
        mainEvaluator.eval(mainParser.parse(getTokens(sourceFile)))
    }

    fun loadMainSource(source: String) {
        mainEvaluator.eval(mainParser.parse(Lexer(source).tokens))
    }

    // called by parsers, parse the included module
    fun addModule(sourceFile: String, name: String): Boolean {
        if (externalParsers[name] != null) return false
        externalParsers[name] = Parser(this).also { it.parse(getTokens(sourceFile)) }
        return true
    }

    // loads the included module and executes it
    fun executeStaticModule(name: String) {
        staticExternalModules[name] = Evaluator(this).also {
            it.eval((externalParsers[name] ?: throw RuntimeException("Static module '$name' not found")).parsed)
        }
    }

    fun getStaticExecutor(name: String) = staticExternalModules[name]

    private fun getTokens(sourceFile: String) = Lexer(File(sourceFile).readText()).tokens
}