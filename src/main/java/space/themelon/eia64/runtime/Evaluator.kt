package space.themelon.eia64.runtime

import space.themelon.eia64.Expression
import space.themelon.eia64.Memory
import space.themelon.eia64.runtime.Entity.Companion.getType
import space.themelon.eia64.runtime.Entity.Companion.unbox
import space.themelon.eia64.syntax.Type
import space.themelon.eia64.syntax.Type.*
import java.util.*
import kotlin.collections.ArrayList

class Evaluator : Expression.Visitor<Any> {
    
    fun eval(expr: Expression) = expr.accept(this)

    private fun safeUnbox(expr: Expression, expectedType: Type, operation: String): Any {
        val result = eval(expr)
        val gotType = getType(result)
        if (gotType != expectedType)
            throw RuntimeException("Expected type $expectedType for [$operation] but got $gotType")
        return unbox(result)
    }
    
    private fun booleanExpr(expr: Expression, operation: String) = safeUnbox(expr, C_BOOL, operation) as Boolean
    private fun intExpr(expr: Expression, operation: String) = safeUnbox(expr, C_INT, operation) as Int
    
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

    private fun define(mutable: Boolean, def: Expression.DefinitionType, value: Any) {
        // make sure variable type = assigned type
        val valueType = getType(value)
        if (def.type != C_ANY && def.type != valueType)
            throw RuntimeException("Variable ${def.name} has type ${def.type}, but got value type of $valueType")
        memory.defineVar(def.name, unbox(value), mutable, valueType)
    }

    private fun update(name: String, value: Any) {
        (memory.get(name) as Entity).update(value)
    }

    override fun variable(variable: Expression.Variable): Any {
        val value = eval(variable.expr)
        define(variable.mutable, variable.definition, value)
        return value
    }

    override fun unaryOperation(expr: Expression.UnaryOperation) = when (val type = operator(expr.operator)) {
        NOT -> !booleanExpr(expr.expr, "! Not")
        NEGATE -> Math.negateExact(intExpr(expr.expr, "- Negate"))
        INCREMENT, DECREMENT -> {
            if (expr.expr !is Expression.Alpha)
                throw RuntimeException("Expected variable type for ${type.name} operation")
            val name = expr.expr.value
            var curr = intExpr(expr.expr, "++ Increment")
            if (expr.left) {
                update(name, if (type == INCREMENT) ++curr else --curr)
                curr
            } else {
                update(name, if (type == INCREMENT) curr + 1 else curr - 1)
                curr
            }
        }
        else -> throw RuntimeException("Unknown unary operator $type")
    }

    override fun binaryOperation(expr: Expression.BinaryOperation) = when (val type = operator(expr.operator)) {
        PLUS -> {
            val left = eval(expr.left)
            val right = eval(expr.right)

            if (getType(left) == C_INT && getType(right) == C_INT)
                unbox(left) as Int + unbox(right) as Int
            else unbox(left).toString() + unbox(right).toString()
        }
        NEGATE -> intExpr(expr.left, "- Subtract") - intExpr(expr.right, "- Subtract")
        ASTERISK -> intExpr(expr.left, "* Multiply") * intExpr(expr.right, "* Multiply")
        SLASH -> intExpr(expr.left, "/ Divide") / intExpr(expr.right, "/ Divide")
        EQUALS, NOT_EQUALS -> {
            var left = eval(expr.left)
            var right = eval(expr.right)
            if (getType(left) != getType(right)) {
                type != EQUALS
            } else {
                left = unbox(left)
                right = unbox(right)

                when (left) {
                    is Int, is String -> if (type == EQUALS) left == right else left != right
                    else -> false
                }
            }
        }
        LOGICAL_AND -> booleanExpr(expr.left, "&& Logical And") && booleanExpr(expr.right, "&& Logical And")
        LOGICAL_OR -> booleanExpr(expr.left, "|| Logical Or") || booleanExpr(expr.right, "|| Logical Or")
        GREATER_THAN -> intExpr(expr.left, "> GreaterThan") > intExpr(expr.right, "> GreaterThan")
        LESSER_THAN -> {
            val left = intExpr(expr.left, "< LesserThan")
            val right = intExpr(expr.right, "< LesserThan")
            left < right
        }
        GREATER_THAN_EQUALS -> intExpr(expr.left, ">= GreaterThanEquals") >= intExpr(expr.right, ">= GreaterThanEquals")
        LESSER_THAN_EQUALS -> intExpr(expr.left, "<= LesserThan") <= intExpr(expr.right, "<= LesserThan")
        ASSIGNMENT -> {
            if (expr.left !is Expression.Alpha)
                throw RuntimeException("[OP =] expected left type to be a name, but got ${expr.left}")
            val name = expr.left.value
            val value = eval(expr.right)
            update(name, value)
            value
        }
        BITWISE_AND -> intExpr(expr.left, "& BitwiseAnd") and intExpr(expr.right, "& BitwiseAnd")
        BITWISE_OR -> intExpr(expr.left, "| BitwiseOr") or intExpr(expr.right, "| BitwiseOr")
        else -> throw RuntimeException("Unknown binary operator $type")
    }

    override fun expressions(list: Expression.ExpressionList): Any {
        for (expression in list.expressions) {
            val result = eval(expression)
            if (result is FlowBlack) {
                return if (result.interrupt == Interrupt.RETURN) result.data!! else result
            }
        }
        return list
    }

    override fun methodCall(call: Expression.MethodCall): Any {
        val fnName = call.name
        val fn = memory.get(fnName)
        if (fn !is Expression.Function)
            throw RuntimeException("Unable to find function $fnName")

        val sigArgsSize = fn.arguments.size
        val callArgsSize = call.arguments.size

        if (sigArgsSize != callArgsSize)
            throw RuntimeException("Expected $sigArgsSize args for function $fnName but got $callArgsSize")

        val parameters = fn.arguments.iterator()
        val callExpressions = call.arguments.expressions.iterator()

        val callValues = ArrayList<Pair<Expression.DefinitionType, Any>>()
        while (parameters.hasNext()) {
            val definedParameter = parameters.next()
            val typeSignature = definedParameter.type

            val callValue = eval(callExpressions.next())
            val gotTypeSignature = getType(callValue)

            if (typeSignature != gotTypeSignature)
                throw RuntimeException("Expected type $typeSignature for arg '${definedParameter.name}' for function $fnName but got $gotTypeSignature")
            callValues.add(Pair(definedParameter, callValue))
        }
        createSubMemory()
        callValues.forEach {
            val definedParameter = it.first
            val value = it.second

            memory.defineVar(definedParameter.name, value, true, definedParameter.type)
        }
        val result = eval(fn.body)
        destroySubMemory()
        if (result is FlowBlack && result.interrupt == Interrupt.RETURN)
            return result.data!!
        return result
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

    override fun forEach(forEach: Expression.ForEach): Any {
        val named = forEach.name
        val iterable = eval(forEach.entity)
        if (iterable is String) {
            for (c in iterable) {
                createSubMemory()
                memory.defineVar(named, c.toString(), false, C_STRING)
                val result = eval(forEach.body)
                destroySubMemory()
                if (result is FlowBlack)
                    when (result.interrupt) {
                        Interrupt.BREAK -> break
                        Interrupt.CONTINUE -> continue
                        Interrupt.RETURN -> return result
                        else -> { }
                    }
            }
        } else throw RuntimeException("Unknown non-interactable type $iterable")
        return forEach
    }

    override fun itr(itr: Expression.Itr): Any {
        val named = itr.name
        var from = intExpr(itr.from, "Itr from")
        val to = intExpr(itr.to, "Itr to")
        var by = if (itr.by == null) 1 else intExpr(itr.by, "Itr by")

        val reverse = from > to
        if (reverse) by = -by

        while (if (reverse) from >= to else from <= to) {
            createSubMemory()
            memory.defineVar(named, from, false, C_INT)
            val result = eval(itr.body)
            destroySubMemory()
            if (result is FlowBlack)
                when (result.interrupt) {
                    Interrupt.BREAK -> break
                    Interrupt.CONTINUE -> continue
                    Interrupt.RETURN -> return result
                    else -> { }
                }
            from += by
        }
        return itr
    }

    override fun forLoop(forLoop: Expression.ForLoop): Any {
        createSubMemory()
        forLoop.initializer?.let { eval(it) }

        val conditional = forLoop.conditional
        while (if (conditional == null) true else booleanExpr(conditional, "ForLoop")) {
            createSubMemory()
            val result = eval(forLoop.body)
            destroySubMemory()
            if (result is FlowBlack) {
                when (result.interrupt) {
                    Interrupt.BREAK -> break
                    Interrupt.CONTINUE -> continue
                    Interrupt.RETURN -> return result
                    else -> { }
                }
            }
            forLoop.operational?.let { eval(it) }
        }

        destroySubMemory()
        return forLoop
    }

    override fun nativeCall(call: Expression.NativeCall): Any {
        val argsSize = call.arguments.size
        when (val type = call.type) {
            PRINT, PRINTLN -> {
                var printCount = 0
                call.arguments.expressions.forEach {
                    val obj = unbox(eval(it)).toString()
                    printCount += obj.length
                    print(obj)
                }
                if (type == PRINTLN) println()
                return printCount
            }

            READ, READLN -> {
                if (argsSize != 0) throw RuntimeException("Expected no arguments for read()/readln(), got $argsSize")
                return Scanner(System.`in`).let { if (type == READ) it.next() else it.nextLine() }
            }

            SLEEP -> {
                if (argsSize != 1) throw RuntimeException("Expected only 1 argument for sleep, got $argsSize")
                val ms = intExpr(call.arguments.expressions[0], "sleep()")
                Thread.sleep(ms.toLong())
                return ms
            }

            LEN -> {
                if (argsSize != 1) throw RuntimeException("Expected only 1 argument for len, got $argsSize")
                return unbox(eval(call.arguments.expressions[0])).toString().length
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
        memory.defineFunc(function.name, function)
        return function
    }

    override fun elementAccess(access: Expression.ElementAccess): Any {
        val entity = eval(access.expr)
        if (getType(entity) == C_STRING) {
            val index = intExpr(access.index, "[] ArrayAccess")
            return (unbox(entity) as String)[index].toString()
        }
        throw RuntimeException("Unknown entity type $entity")
    }
}