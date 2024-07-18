package space.themelon.eia64

import space.themelon.eia64.signatures.ArrayExtension
import space.themelon.eia64.signatures.Matching
import space.themelon.eia64.signatures.ObjectExtension
import space.themelon.eia64.signatures.Sign

object MatchTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val a = ObjectExtension("NullA")
        val b = ObjectExtension("NullB")

        println(Matching.matches(a, b))

        val c = ArrayExtension(Sign.INT)
        val d = ArrayExtension(Sign.STRING)

        println(Matching.matches(c, d))
    }
}