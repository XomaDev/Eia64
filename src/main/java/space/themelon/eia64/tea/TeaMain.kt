package space.themelon.eia64.tea

import org.teavm.jso.browser.Window
import org.teavm.jso.dom.html.HTMLButtonElement
import org.teavm.jso.dom.html.HTMLDocument

object TeaMain {

    private val document: HTMLDocument = Window.current().document
    private val meowButton: HTMLButtonElement = document.getElementById("meow-button").cast()

    @JvmStatic
    fun main(args: Array<String>) {
        println("Meow, World!")
        meowButton.listenClick {
            println("Button Clicked!")
        }
    }
}