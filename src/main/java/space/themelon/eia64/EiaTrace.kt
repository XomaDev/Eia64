package space.themelon.eia64

import space.themelon.eia64.signatures.Signature
import java.io.PrintStream
import java.util.*

class EiaTrace(
    private val output: PrintStream
) {

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

        const val GOLD: String = "\u001b[38;2;255;215;0m"
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
        output.println("$spaces$BLUE$BOLD[f] fun$RESET $BOLD$name$RESET ( $argsText )")
    }

    // Used at parse time!
    fun declareVariable(
        mutable: Boolean,
        name: String,
        signature: Signature
    ) {
        // [v] const? variable name signature
        output.println(
            "$spaces${YELLOW}$BOLD[v] ${if (mutable) "" else "const "}" +
                    "variable$RESET $BOLD$name$RESET ${signature.logName()}"
        )
    }

    // Used at runtime! It also displays the values
    fun declareVariableRuntime(
        mutable: Boolean,
        name: String,
        signature: Signature,
        value: Any
    ) {
        // [v] const? variable name signature
        output.println(
            "$spaces${YELLOW}$BOLD[v] ${if (mutable) "" else "const "}" +
                    "variable$RESET $BOLD$name$RESET ${signature.logName()} $BLUE$BOLD$value$RESET"
        )
    }

    // When a variable is being accessed from memory! Used at runtime!
    fun getVariableRuntime(
        name: String,
        signature: Signature,
        value: Any
    ) {
        output.println(
            "$spaces$GOLD$BOLD[get-var] get() $name$RESET $BOLD${signature.logName()}$RESET = $BLUE$BOLD$value$RESET"
        )
    }

    // When a variable is being updated from memory!
    // Used at runtime!
    // This does not include those primitives like EInt where value
    // gets modified directly, internally
    fun updateVariableRuntime(
        name: String,
        signature: Signature,
        value: Any
    ) {
        output.println(
            "$spaces$GREEN$BOLD[set-var] set() $name$RESET $BOLD${signature.logName()}$RESET = $BLUE$BOLD$value$RESET"
        )
    }

    fun runtimeFnCall(
        name: String,
        args: List<Pair<String, Any>>
    ) {
        val argsText = StringJoiner(", ")

        args.forEach {
            val parameterName = it.first
            val parameter = it.second

            argsText.add("$parameterName = $parameter")
        }

        output.println(
            "$spaces $BLUE$BOLD[call-fn] function $name$RESET $BOLD( $argsText )$RESET"
        )
    }

    fun runtimeUntil(
        iteration: Int
    ) {
        output.println(
            "$spaces $PURPLE$BOLD[until]$RESET ${BOLD}iteration $iteration$RESET"
        )
    }

    // TODO:
    //  in future we need to add more metadata, what are the conditions?
    //  what are the initializers, operationals, conditionals etc
    fun runtimeFor(
        iteration: Int,
    ) {
        output.println(
            "$spaces $PURPLE$BOLD[for]$RESET$BOLD iteration $iteration$RESET"
        )
    }

    fun runtimeForEach(
        iteration: Int,
        iterable: Any,
        element: Any // current element
    ) {
        output.println(
            "$spaces $CYAN$BOLD[for each]$RESET $BOLD$iterable$RESET iteration $iteration -> $BOLD$element$RESET"
        )
    }

    fun runtimeObjectCreation(
        module: String,
        value: Any,
    ) {
        output.println(
            "$spaces $BLUE$BOLD[new]$RESET $BOLD$module$RESET = $BOLD$value$RESET"
        )
    }
}