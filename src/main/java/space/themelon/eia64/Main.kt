package space.themelon.eia64

import space.themelon.eia64.syntax.SyntaxAnalysis

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val loader = javaClass.classLoader
        val source = loader.getResource("hello.eia")!!.readText()

        val tokens = SyntaxAnalysis().tokenize(source)

        val evaluator = Evaluator(Memory())
        Parser(tokens).expressions.forEach { evaluator.eval(it) }
    }
}