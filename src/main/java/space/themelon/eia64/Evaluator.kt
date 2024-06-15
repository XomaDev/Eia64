package space.themelon.eia64

import space.themelon.eia64.syntax.Type
import java.util.StringJoiner

class Evaluator(
    private val memory:  Memory
): Expression.Visitor<Any> {

    private var currentMemory = memory

    override fun eBool(bool: Expression.EBool) = bool.value
    override fun eInt(eInt: Expression.EInt) = eInt.value
    override fun eString(eString: Expression.EString) = eString.value
    override fun operator(operator: Expression.Operator) = operator.value

    override fun alpha(alpha: Expression.Alpha): Any {
        return memory.get(alpha.value)
    }

    override fun binaryOperation(operation: Expression.BinaryOperation): Any {
        val left = eval(operation.left)
        val right = eval(operation.right)

        val operator = eval(operation.operator)
        return when (operator) {
            Type.PLUS -> {
                if (left is Int && right is Int)
                    left + right
                else left.toString() + right.toString()
            }
            Type.NEGATE -> {
                left as Int - right as Int
            }
            Type.ASTERISK -> {
                left as Int * right as Int
            }
            Type.SLASH -> {
                left as Int / right as Int
            }
            else -> {
                throw RuntimeException("Unknown operator $operation")
            }
        }
    }

    override fun variable(variable: Expression.Variable): Any {
        val content = eval(variable.expr)
        currentMemory.define(variable.name, content)
        return content
    }

    override fun methodCall(call: Expression.MethodCall): Any {
        if (call.name == "println") {
            val joined = StringJoiner(", ")
            call.arguments.forEach { joined.add(eval(it).toString()) }
            println(joined)
            return joined.length()
        }
        return "<>"
    }

    fun eval(expression: Expression) = expression.accept(this)
}