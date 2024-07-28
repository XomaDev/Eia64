package space.themelon.eia64

import space.themelon.eia64.runtime.Executor
import space.themelon.eia64.runtime.Nothing
import java.util.*

object EiaLive {
    @JvmStatic
    fun main(args: Array<String>) {
        val scanner = Scanner(System.`in`)
        val executor = Executor()

        var buffer = StringJoiner("\n")
        while (true) {
            print("> ")
            val line = scanner.nextLine()
            if (line == "exit") break
            else if (line == "~~") {
                val result = executor.loadMainSource(buffer.toString())
                if (result !is Nothing) {
                    println(result)
                }
                buffer = StringJoiner("\n")
            }
            else buffer.add(line)
        }
    }
}