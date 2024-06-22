package space.themelon.eia64

import space.themelon.eia64.runtime.Executor

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val source = javaClass.classLoader.getResource("trytobreak.eia").file

        val executor = Executor()
        val startTime = System.nanoTime()
        executor.loadFile(source)
        println("Took " + (System.nanoTime() - startTime) + " ns")
    }
}