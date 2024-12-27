package space.themelon.eia64.runtime

import space.themelon.eia64.analysis.Parser
import space.themelon.eia64.expressions.ExpressionList
import space.themelon.eia64.mirror.DefaultImports
import space.themelon.eia64.syntax.Lexer
import java.io.File
import kotlin.system.exitProcess

class Environment {

    companion object {
        var DEBUG = true
        var EIA_SHUTDOWN: (Int) -> Unit =  { exitProcess(it) }
    }

    var standardOutput = System.out
    var standardInput = System.`in`

    var classes = mutableMapOf<String, Class<*>>()

    private val evaluator = Evaluator("Main", this)
    private val parser = Parser(this)

    init {
        DefaultImports.defaultClassNames.withIndex().forEach { (index, clazz) ->
           classes += clazz to DefaultImports.defaultClasses[index]
        }
    }

    fun parse(source: String) = parser.parse(Lexer(source).tokens)
    fun parse(file: File) = parse(file.readText())

    fun evaluate(expressions: ExpressionList) = evaluator.eval(expressions)

    fun clearMemory() {
        parser.reset()
        evaluator.clearMemory()
    }
}