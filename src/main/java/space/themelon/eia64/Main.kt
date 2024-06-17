package space.themelon.eia64

import space.themelon.eia64.runtime.Evaluator
import space.themelon.eia64.syntax.SyntaxAnalysis
import java.io.File

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val source = (File(System.getProperty("user.dir"), args[0])).readText()

        val tokens = SyntaxAnalysis().tokenize(source)

        val evaluator = Evaluator()
        val startTime = System.nanoTime()
        evaluator.eval(Parser(tokens).parsedResult)
        println("Took " + (System.nanoTime() - startTime) + " ns")
    }
}