package space.themelon.eia64

import space.themelon.eia64.signatures.Signature
import java.util.StringJoiner

class EiaTrace {

    companion object {
        private const val RESET: String = "\u001b[0m"
        private const val BLACK: String = "\u001b[0;30m"
        private const val RED: String = "\u001b[0;31m"
        private const val GREEN: String = "\u001b[0;32m"
        private const val YELLOW: String = "\u001b[0;33m"
        private const val BLUE: String = "\u001b[0;34m"
        private const val PURPLE: String = "\u001b[0;35m"
        private const val CYAN: String = "\u001b[0;36m"
        private const val WHITE: String = "\u001b[0;37m"
        private const val BOLD: String = "\u001b[1m"
        private const val UNDERLINE: String = "\u001b[4m"
    }

    private var scope = 0

    fun enterScope() {
        scope++
    }

    fun leaveScope() {
        scope--
    }

    private val spaces: String
        get() = " ".repeat(scope)

    fun declareVariable(
        mutable: Boolean,
        name: String,
        signature: Signature
    ) {
        // [v] const? variable name signature
        println("$spaces${YELLOW}$BOLD[v] ${if (mutable) "" else "const "}variable$RESET $BOLD$name$RESET ${signature.logName()}")
    }

    fun declareFn(
        name: String,
        parameters: List<Pair<String, Signature>>,
    ) {
        val argsText = StringJoiner(", ")

        parameters.forEach {
            val parameterName = it.first
            val signature = it.second.logName()

            argsText.add("$BOLD$parameterName$RESET $signature")
        }
        // [f] fun param_name param_signature
        println("$spaces$BLUE$BOLD[f] fun$RESET $BOLD$name$RESET ( $argsText )")
    }
}