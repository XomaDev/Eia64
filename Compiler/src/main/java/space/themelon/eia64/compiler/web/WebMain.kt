package space.themelon.eia64.compiler.web

import org.teavm.jso.dom.html.HTMLDocument
import org.teavm.jso.dom.html.HTMLTextAreaElement


object WebMain {
    @JvmStatic
    fun main(args: Array<String>) {
        HTMLDocument.current().getElementById("submitBtn")
            .addEventListener("click") {
                val textArea: HTMLTextAreaElement =
                    HTMLDocument.current().getElementById("codeInput") as HTMLTextAreaElement
                val code = textArea.value
                println(code)

            }
    }
}