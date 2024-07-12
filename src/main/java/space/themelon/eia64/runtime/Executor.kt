package space.themelon.eia64.runtime

import space.themelon.eia64.analysis.Parser
import space.themelon.eia64.syntax.Lexer
import java.io.File

class Executor {

    companion object {
        var STD_LIB = "" // will be set
        var EXECUTION_DIRECTORY: String = File(System.getProperty("user.dir")).absolutePath
    }

    init {
        if (STD_LIB.isBlank()) throw RuntimeException("STD_LIB is not set")
    }


    // why do we do this? sometimes while we are developing demonstrable
    // APIs for Eia64, we would want the output to be captured in memory and
    // sent somewhere else
    var STANDARD_OUTPUT = System.out
    var allowInput = true

    private val externalExecutors = HashMap<String, Evaluator>()
    private val mainEvaluator = Evaluator("Main", this)

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

    fun getModule(name: String) = externalParsers[name] ?: throw RuntimeException("Could not find module '$name'")

    // loads the included module and executes it
    fun executeModule(name: String): Evaluator {
        val evaluator = newEvaluator(name)
        externalExecutors[name] = evaluator
        return evaluator
    }

    fun newEvaluator(name: String) = Evaluator(name, this).also {
        it.eval((externalParsers[name] ?: throw RuntimeException("Static module '$name') not found")).parsed)
    }

    fun getEvaluator(name: String) = externalExecutors[name]

    private fun getTokens(sourceFile: String) = Lexer(File(sourceFile).readText()).tokens
}