package space.themelon.eia64

object KotlinHandling {
    @JvmStatic
    fun main(args: Array<String>) {
        val a = aKotlinFunc()
        println(a)
        println(a)
        // kotlin returns a unit
        //Integer.parseInt()
    }

    private fun aKotlinFunc() {
        println("Hello world")
    }
}