package space.themelon.eia64

import space.themelon.eia64.syntax.LiveUnit
import java.util.*

object EiaLiveOld {
    @JvmStatic
    fun main(args: Array<String>) {
        // We need to simulate how it would work
        // When there are incomplete tokens, eia should wait before executing them
        val scanner = Scanner(System.`in`)
        val live = LiveUnit()
        while (true) {
            print("eia> ")
            val line = scanner.nextLine()
            live.feedLine(line)
        }
    }
}