package space.themelon.eia64.evaluate

import space.themelon.eia64.Expression
import space.themelon.eia64.Memory
import space.themelon.eia64.syntax.Type.*

class Evaluator : Expression.Visitor<Any> {
    
    fun eval(expr: Expression) = expr.accept(this)
    
    private fun booleanExpr(expr: Expression, operation: String) : Boolean {
        val value = expr.accept(this)
        if (value is Boolean) return value
        throw IllegalArgumentException("Expected boolean type for [$operation], but got $value")
    }
    
    private fun intExpr(expr: Expression, operation: String) : Int {
        val value = expr.accept(this)
        if (value is Int) return value
        throw IllegalArgumentException("Expected int type for [$operation], but got $value")
    }
    
    private var memory = Memory()

    private fun createSubMemory() {
        memory = if (memory.next != null) memory.next!! else Memory(memory)
    }

    private fun destroySubMemory() {
        if (memory.isLower()) memory = memory.superMemory()
        else throw RuntimeException("Already a super memory")
    }

    override fun eBool(bool: Expression.EBool) = bool.value
    override fun eInt(eInt: Expression.EInt) = eInt.value
    override fun eString(eString: Expression.EString) = eString.value
    override fun alpha(alpha: Expression.Alpha) = memory.get(alpha.value)
    override fun operator(operator: Expression.Operator) = operator.value

    override fun unaryOperation(expr: Expression.UnaryOperation) = when (val type = operator(expr.operator)) {
        NOT -> !booleanExpr(expr.expr, "! Not")
        NEGATE -> Math.negateExact(intExpr(expr.expr, "- Negate"))
        INCREMENT, DECREMENT -> {
            if (expr.expr !is Expression.Alpha)
                throw RuntimeException("Expected variable type for ${type.name} operation")
            val name = expr.expr.value
            var curr = intExpr(expr.expr, "++ Increment")
            if (expr.left) {
                memory.update(name, if (type == INCREMENT) ++curr else --curr)
                curr
            } else {
                memory.update(name, if (type == INCREMENT) curr + 1 else curr - 1)
                curr
            }
        }
        else -> throw RuntimeException("Unknown unary operator $type")
    }

    override fun binaryOperation(expr: Expression.BinaryOperation) = when (val type = operator(expr.operator)) {
        PLUS -> {
            val left = eval(expr.left)
            val right = eval(expr.right)
            if (left is Int && right is Int) left + right
            else left.toString() + right.toString()
        }
        NEGATE -> intExpr(expr.left, "- Subtract") - intExpr(expr.right, "- Subtract")
        ASTERISK -> intExpr(expr.left, "* Multiply") * intExpr(expr.right, "* Multiply")
        SLASH -> intExpr(expr.left, "/ Divide") / intExpr(expr.right, "/ Divide")
        EQUALS, NOT_EQUALS -> {
            val left = eval(expr.left)
            val right = eval(expr.right)
            if (left::class != right::class) type != EQUALS
            else when (left) {
                is Int, is String -> if (type == EQUALS) left == right else left != right
                else -> false
            }
        }
        LOGICAL_AND -> booleanExpr(expr.left, "&& Logical And") && booleanExpr(expr.right, "&& Logical And")
        LOGICAL_OR -> booleanExpr(expr.left, "|| Logical Or") || booleanExpr(expr.right, "|| Logical Or")
        GREATER_THAN -> intExpr(expr.left, "> GreaterThan") > intExpr(expr.right, "> GreaterThan")
        LESSER_THAN -> intExpr(expr.left, "< LesserThan") < intExpr(expr.right, "< LesserThan")
        GREATER_THAN_EQUALS -> intExpr(expr.left, ">= GreaterThanEquals") >= intExpr(expr.right, ">= GreaterThanEquals")
        LESSER_THAN_EQUALS -> intExpr(expr.left, "<= LesserThan") <= intExpr(expr.right, "<= LesserThan")
        ASSIGNMENT -> {
            if (expr.left !is Expression.Alpha)
                throw RuntimeException("[OP =] expected left type to be a name, but got ${expr.left}")
            val name = expr.left.value
            val value = eval(expr.right)
            memory.update(name, value)
            value
        }
        BITWISE_AND -> intExpr(expr.left, "& BitwiseAnd") and intExpr(expr.right, "& BitwiseAnd")
        BITWISE_OR -> intExpr(expr.left, "| BitwiseOr") or intExpr(expr.right, "| BitwiseOr")
        else -> throw RuntimeException("Unknown binary operator $type")
    }

    override fun variable(variable: Expression.Variable): Any {
        val value = eval(variable.expr)
        memory.define(variable.name, value)
        return value
    }

    override fun expressions(list: Expression.ExpressionList): Any {
        for (expression in list.expressions) {
            val result = eval(expression)
            if (result is FlowBlack) return result
        }
        return list
    }

    override fun methodCall(call: Expression.MethodCall): Any {
        val name = call.name
        val fn = memory.get(name)
        if (fn !is Expression.Function) throw RuntimeException("Expected function type, got $fn")
        val expectedArgsSize = fn.arguments.size
        val gotArgsSize = call.arguments.size
        if (expectedArgsSize != gotArgsSize) throw RuntimeException("Expected $expectedArgsSize, but got $gotArgsSize for fn $name")

        val names = fn.arguments.iterator()
        val values = call.arguments.expressions.iterator()

        createSubMemory()
        while (names.hasNext()) memory.define(names.next(), eval(values.next()))
        val result = eval(fn.body)
        destroySubMemory()
        if (result is FlowBlack && result.interrupt == Interrupt.RETURN)
            return result.data!!
        return Interrupt.NONE
    }

    override fun until(until: Expression.Until): Any {
        while (booleanExpr(until.expression, "Until Condition")) {
            createSubMemory()
            val result = eval(until.body)
            destroySubMemory()
            if (result is FlowBlack)
                when (result.interrupt) {
                    Interrupt.BREAK -> break
                    Interrupt.CONTINUE -> continue
                    Interrupt.RETURN -> return result
                    else -> { }
                }
        }
        return until
    }

    override fun nativeReadWrite(call: Expression.NativeReadWrite): Any {
        val argsSize = call.arguments.size
        when (val type = call.type) {
            F_OUT -> {
                if (argsSize != 1) throw RuntimeException("Expected only 1 argument for fout, got $argsSize")
                val obj = eval(call.arguments.expressions[0]).toString()
                println(obj)
                return obj.length
            }
            else -> throw RuntimeException("Unknown native read write $type")
        }
    }

    override fun interruption(interruption: Expression.Interruption) = when (val type = eval(interruption.type)) {
        RETURN -> FlowBlack(Interrupt.RETURN, eval(interruption.expr!!))
        BREAK -> FlowBlack(Interrupt.BREAK)
        CONTINUE -> FlowBlack(Interrupt.CONTINUE)
        else -> throw RuntimeException("Unknown interruption type $type")
    }

    override fun ifFunction(ifExpr: Expression.If): Any {
        val body = if (booleanExpr(ifExpr.condition, "If Condition")) ifExpr.thenBranch else ifExpr.elseBranch
        if (body != null) {
            createSubMemory()
            val result = eval(body)
            destroySubMemory()
            return result
        }
        return ifExpr
    }

    override fun function(function: Expression.Function): Any {
        memory.define(function.name, function)
        return function
    }
}