package space.themelon.eia64.evaluate

import space.themelon.eia64.Expression
import space.themelon.eia64.Memory
import space.themelon.eia64.syntax.Type

class Evaluator(
    memory: Memory
) : Expression.Visitor<Any> {

    private var currentMemory = memory

    private fun darkMemory() {
        currentMemory = if (currentMemory.next != null) currentMemory.next!! else Memory(currentMemory)
    }

    private fun lightMemory() {
        if (currentMemory.isLower()) currentMemory = currentMemory.superMemory()
        else throw RuntimeException("Already a super memory")
    }

    fun evalList(list: List<Expression>): ExpressionResult {
        val results = ArrayList<Any>(list.size)
        for (expression in list) {
            val result = eval(expression)
            results.add(result)
            if (result is ExpressionResult && result.result == Result.RETURN)
                return ExpressionResult(Result.RETURN, results)
        }
        return ExpressionResult(Result.OK, results)
    }

    private fun eval(expression: Expression) = expression.accept(this)

    private fun evalInt(expression: Expression, action: String): Int {
        val result = eval(expression)
        if (result is Int) return result
        throw RuntimeException("Expected Int for operation $action but got ${result.javaClass.simpleName}")
    }

    private fun evalBoolean(expression: Expression, action: String): Boolean {
        val result = eval(expression)
        if (result is Boolean) return result
        throw RuntimeException("Expected Boolean for [$action] but got ${result.javaClass.simpleName}")
    }

    override fun eBool(bool: Expression.EBool) = bool.value
    override fun eInt(eInt: Expression.EInt) = eInt.value
    override fun eString(eString: Expression.EString) = eString.value
    override fun operator(operator: Expression.Operator) = operator.value
    override fun alpha(alpha: Expression.Alpha) = currentMemory.get(alpha.value)

    override fun unaryOperation(expr: Expression.UnaryOperation) = when (val operator = eval(expr.operator)) {
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

    override fun binaryOperation(expr: Expression.BinaryOperation) = when (val operator = eval(expr.operator)) {
        Type.PLUS -> {
            val left = eval(expr.left)
            val right = eval(expr.right)

            if (left is Int && right is Int) left + right
            else left.toString() + right.toString()
        }
        Type.NEGATE -> {
            evalInt(expr.left, "-") - evalInt(expr.right, "-")
        }

        Type.ASTERISK -> {
            evalInt(expr.left, "*") * evalInt(expr.right, "*")
        }

        Type.SLASH -> {
            evalInt(expr.left, "/") / evalInt(expr.right, "/")
        }

        Type.EQUALS, Type.NOT_EQUALS -> {
            val left = eval(expr.left)
            val right = eval(expr.right)

            if (left::class != right::class) {
                operator != Type.EQUALS
            }
            when (left) {
                is Int, is String -> {
                    if (operator == Type.EQUALS) left == right else left != right
                }
                else -> false
            }
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

    override fun variable(variable: Expression.Variable): Any {
        val content = eval(variable.expr)
        currentMemory.define(variable.name, content)
        return content
    }

    override fun expressionList(exprList: Expression.ExpressionList): Any {
        val results = evalList(exprList.expressions).value as List<*>
        return results[results.size - 1]!!
    }

    override fun methodCall(call: Expression.MethodCall): Any {
        val fnName = call.name
        val fn = currentMemory.get(fnName)
        if (fn !is Expression.Function) throw RuntimeException("Unable to find function $fnName")
        if (fn.arguments.size != call.arguments.size) throw RuntimeException("Function call $fnName, args size mismatch")

        darkMemory()

        val namesItr = fn.arguments.expressions.iterator()
        val exprsIter = call.arguments.expressions.iterator()

        while (namesItr.hasNext())
            currentMemory.define((namesItr.next() as Expression.Alpha).value, eval(exprsIter.next()))

        val result = eval(fn.body)
        lightMemory()
        return if (result is ExpressionResult && result.value != null) result.value else result
    }

    override fun nativeCall(call: Expression.NativeCall): Any {
        if (call.type == Type.F_OUT) {
            if (call.arguments.size != 1)
                throw RuntimeException("Wrong number of arguments for native call $call")
            val toPrint = eval(call.arguments.expressions[0]).toString()
            println(toPrint)
            return toPrint.length
        }
        return 0
    }

    override fun ifFunction(ifDecl: Expression.IfFunction): Any {
        val condition = evalBoolean(ifDecl.condition, "If")

        darkMemory()
        val result = if (condition) eval(ifDecl.thenBranch) else {
            if (ifDecl.elseBranch != null) eval(ifDecl.elseBranch)
            else ExpressionResult(Result.NONE, "no_else_branch")
        }
        lightMemory()
        return result
    }

    override fun function(function: Expression.Function): Any {
        currentMemory.define(function.name, function)
        return function
    }

    override fun interruption(interruption: Expression.Interruption): Any {
        val result = when (val type = eval(interruption.type)) {
            Type.RETURN -> Result.RETURN
            Type.BREAK -> Result.BREAK
            Type.CONTINUE -> Result.CONTINUE
            else -> throw RuntimeException("Unknown interruption $type")
        }
        return if (interruption.expr == null) ExpressionResult(result)
        else ExpressionResult(result, eval(interruption.expr))
    }
}