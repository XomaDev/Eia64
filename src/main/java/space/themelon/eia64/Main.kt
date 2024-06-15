package space.themelon.eia64

import space.themelon.eia64.evaluate.Evaluator
import space.themelon.eia64.syntax.SyntaxAnalysis

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val loader = javaClass.classLoader
        val source = loader.getResource("hello.eia")!!.readText()

        val tokens = SyntaxAnalysis().tokenize(source)

        val evaluator = Evaluator(Memory())
        val startTime = System.nanoTime()
        evaluator.evalList(Parser(tokens).expressions)
        println("Took " + (System.nanoTime() - startTime) + " ns")
    }
}