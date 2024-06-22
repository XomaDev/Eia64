package space.themelon.eia64

import space.themelon.eia64.analysis.Parser
import space.themelon.eia64.runtime.Evaluator
import space.themelon.eia64.syntax.SyntaxAnalysis

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val source = javaClass.classLoader.getResource("fixtest.eia").readText()

        val tokens = SyntaxAnalysis().tokenize(source)

        val evaluator = Evaluator()
        val startTime = System.nanoTime()
        evaluator.eval(Parser().parse(tokens))
        println("Took " + (System.nanoTime() - startTime) + " ns")
    }
}