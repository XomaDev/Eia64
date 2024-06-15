package space.themelon.eia64

import space.themelon.eia64.syntax.Type
import java.util.StringJoiner

class Evaluator(
    private val memory: Memory
) : Expression.Visitor<Any> {

    private var currentMemory = memory

    override fun eBool(bool: Expression.EBool) = bool.value
    override fun eInt(eInt: Expression.EInt) = eInt.value
    override fun eString(eString: Expression.EString) = eString.value
    override fun operator(operator: Expression.Operator) = operator.value

    override fun alpha(alpha: Expression.Alpha): Any {
        return memory.get(alpha.value)
    }

    override fun binaryOperation(operation: Expression.BinaryOperation): Any {
        val operator = eval(operation.operator)

        if (operator == Type.PLUS) {
            val left = eval(operation.left)
            val right = eval(operation.right)

            return if (left is Int && right is Int) left + right
            else left.toString() + right.toString()
        } else if (operator == Type.EQUALS || operator == Type.NOT_EQUALS) {
            val left = eval(operation.left)
            val right = eval(operation.right)

            if (left::class != right::class) {
                return operator != Type.EQUALS
            }
            return when (left) {
                is Int, is String -> {
                    if (operator == Type.EQUALS) left == right else left != right
                }
                else -> false
            }
        }

        return when (operator) {
            Type.NEGATE -> {
                evalInt(operation.left, "-") - evalInt(operation.right, "-")
            }

            Type.ASTERISK -> {
                evalInt(operation.left, "*") * evalInt(operation.right, "*")
            }

            Type.SLASH -> {
                evalInt(operation.left, "/") / evalInt(operation.right, "/")
            }

            Type.LOGICAL_AND -> {
                evalBoolean(operation.left, "&&") && evalBoolean(operation.right, "&&")
            }

            Type.LOGICAL_OR -> {
                evalBoolean(operation.left, "||") || evalBoolean(operation.right, "||")
            }

            Type.GREATER_THAN -> {
                evalInt(operation.left, ">") > evalInt(operation.right, ">")
            }

            Type.LESSER_THAN -> {
                evalInt(operation.left, "<") < evalInt(operation.right, "<")
            }

            Type.GREATER_THAN_EQUALS -> {
                evalInt(operation.left, ">=") >= evalInt(operation.right, ">=")
            }

            Type.LESSER_THAN_EQUALS -> {
                evalInt(operation.left, "<=") <= evalInt(operation.right, "<=")
            }

            Type.BITWISE_AND -> {
                evalInt(operation.left, "&") and evalInt(operation.right, "&")
            }

            Type.BITWISE_OR -> {
                evalInt(operation.left, "|") or evalInt(operation.right, "|")
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

    private fun evalInt(expression: Expression, action: String): Int {
        val result = eval(expression)
        if (result is Int) return result
        throw RuntimeException("Expected Int for operation $action but got ${result.javaClass.simpleName}")
    }

    private fun evalBoolean(expression: Expression, action: String): Boolean {
        val result = eval(expression)
        if (result is Boolean) return result
        throw RuntimeException("Expected Boolean for operation $action but got ${result.javaClass.simpleName}")
    }
}