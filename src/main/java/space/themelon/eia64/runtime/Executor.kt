package space.themelon.eia64.runtime

import space.themelon.eia64.analysis.Parser
import space.themelon.eia64.syntax.SyntaxAnalysis
import java.io.File

class Executor {

    companion object {
        var STD_LIB = ""
    }

    init {
        if (STD_LIB.isBlank())
            throw RuntimeException("STD_LIB is not set")
    }

    private val externalEvaluator = HashMap<String, Evaluator>()

    private val syntaxAnalysis = SyntaxAnalysis()
    private val parser = Parser()

    private val mainEvaluator = Evaluator(this)

    fun loadFile(sourceFile: String) {
        mainEvaluator.eval(parser.parse(getTokens(sourceFile)))
    }

    fun loadSource(source: String) {
        mainEvaluator.eval(parser.parse(getTokens(source)))
    }

    fun loadExternal(sourceFile: String, name: String) {
        if (externalEvaluator[name] != null) return
        Evaluator(this).apply {
            externalEvaluator[name] = this
            eval(parser.parse(getTokens(sourceFile)))
        }
    }

    fun getExternalExecutor(name: String) = externalEvaluator[name]

    private fun getTokens(sourceFile: String) = syntaxAnalysis.tokenize(File(sourceFile).readText())
}