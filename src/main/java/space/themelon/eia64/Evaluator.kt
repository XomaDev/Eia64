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

    override fun unaryOperation(expr: Expression.UnaryOperation): Any {
        return when (val operator = eval(expr.operator)) {
            Type.NOT -> {
                !evalBoolean(expr.expr, "! [Not]")
            }

            Type.NEGATE -> {
                Math.negateExact(evalInt(expr.expr, "- [Negate]"))
            }

            else -> {
                throw RuntimeException("Unknown unary operator $operator")
            }
        }
    }

    override fun binaryOperation(expr: Expression.BinaryOperation): Any {
        val operator = eval(expr.operator)

        if (operator == Type.PLUS) {
            val left = eval(expr.left)
            val right = eval(expr.right)

            return if (left is Int && right is Int) left + right
            else left.toString() + right.toString()
        } else if (operator == Type.EQUALS || operator == Type.NOT_EQUALS) {
            val left = eval(expr.left)
            val right = eval(expr.right)

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
                evalInt(expr.left, "-") - evalInt(expr.right, "-")
            }

            Type.ASTERISK -> {
                evalInt(expr.left, "*") * evalInt(expr.right, "*")
            }

            Type.SLASH -> {
                evalInt(expr.left, "/") / evalInt(expr.right, "/")
            }

            Type.LOGICAL_AND -> {
                evalBoolean(expr.left, "&&") && evalBoolean(expr.right, "&&")
            }

            Type.LOGICAL_OR -> {
                evalBoolean(expr.left, "||") || evalBoolean(expr.right, "||")
            }

            Type.GREATER_THAN -> {
                evalInt(expr.left, ">") > evalInt(expr.right, ">")
            }

            Type.LESSER_THAN -> {
                evalInt(expr.left, "<") < evalInt(expr.right, "<")
            }

            Type.GREATER_THAN_EQUALS -> {
                evalInt(expr.left, ">=") >= evalInt(expr.right, ">=")
            }

            Type.LESSER_THAN_EQUALS -> {
                evalInt(expr.left, "<=") <= evalInt(expr.right, "<=")
            }

            Type.BITWISE_AND -> {
                evalInt(expr.left, "&") and evalInt(expr.right, "&")
            }

            Type.BITWISE_OR -> {
                evalInt(expr.left, "|") or evalInt(expr.right, "|")
            }

            else -> {
                throw RuntimeException("Unknown operator $expr")
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