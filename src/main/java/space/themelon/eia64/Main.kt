package space.themelon.eia64

import space.themelon.eia64.runtime.Executor

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val source = "/home/kumaraswamy/Documents/Eia64/src/main/resources/breakittest.eia"
        // lowercase(), uppercase(), replace()
        // split()

        Executor.STD_LIB = "/home/kumaraswamy/Documents/Eia64/stdlib/"

        val executor = Executor()
        val startTime = System.nanoTime()
        executor.loadFile(source)
        println("Took " + (System.nanoTime() - startTime) + " ns")
    }
}