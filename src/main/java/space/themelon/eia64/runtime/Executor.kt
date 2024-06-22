package space.themelon.eia64.runtime

import space.themelon.eia64.analysis.Parser
import space.themelon.eia64.syntax.SyntaxAnalysis
import java.io.File

class Executor {

    companion object {
        const val STD_LIB = "/home/kumaraswamy/Documents/Eia64/stdlib/"
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
        Evaluator(this).apply {
            externalEvaluator[name] = this
            eval(parser.parse(getTokens(sourceFile)))
        }
    }

    fun getExternalExecutor(name: String) = externalEvaluator[name]

    private fun getTokens(sourceFile: String) = syntaxAnalysis.tokenize(File(sourceFile).readText())
}