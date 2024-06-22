package space.themelon.eia64

import space.themelon.eia64.analysis.Parser
import space.themelon.eia64.runtime.Evaluator
import space.themelon.eia64.syntax.SyntaxAnalysis
import java.util.*

object EiaLive {
    @JvmStatic
    fun main(args: Array<String>) {
        val scanner = Scanner(System.`in`)

        val analysis = SyntaxAnalysis()
        val parser = Parser()
        val executor = Evaluator()

        var buffer = StringJoiner("\n")
        while (true) {
            print("> ")
            val line = scanner.nextLine()
            if (line == "exit") break
            else if (line == "~~") {
                executor.eval(parser.parse(analysis.tokenize(buffer.toString())))
                buffer = StringJoiner("\n")
            }
            else buffer.add(line)
        }
    }
}