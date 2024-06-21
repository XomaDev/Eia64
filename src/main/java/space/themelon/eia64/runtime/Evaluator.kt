package space.themelon.eia64.runtime

import space.themelon.eia64.Expression
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

    private val memory = Memory()

    override fun literal(literal: Expression.Literal) = literal.data
    override fun alpha(alpha: Expression.Alpha) = memory.getVar(alpha.index, alpha.value)
    override fun operator(operator: Expression.Operator) = operator.value

    private fun define(mutable: Boolean, def: Expression.DefinitionType, value: Any) {
        // make sure variable type = assigned type
        val valueType = getType(value)
        if (def.type != C_ANY && def.type != valueType)
            throw RuntimeException("Variable ${def.name} has type ${def.type}, but got value type of $valueType")
        memory.declareVar(def.name, Entity(def.name, mutable, value, valueType))
    }

    private fun update(scope: Int, name: String, value: Any) {
        (memory.getVar(scope, name) as Entity).update(value)
    }

    override fun variable(variable: Expression.ExplicitVariable): Any {
        val value = eval(variable.expr)
        define(variable.mutable, variable.definition, value)
        return value
    }

    override fun autoVariable(autoVariable: Expression.AutoVariable): Any {
        val value = eval(autoVariable.expr)
        memory.declareVar(autoVariable.name, Entity(autoVariable.name, true, unbox(value), getType(value)))
        return value
    }

    override fun unaryOperation(expr: Expression.UnaryOperation) = when (val type = operator(expr.operator)) {
        NOT -> !booleanExpr(expr.expr, "! Not")
        NEGATE -> Math.negateExact(intExpr(expr.expr, "- Negate"))
        INCREMENT, DECREMENT -> {
            if (expr.expr !is Expression.Alpha)
                throw RuntimeException("Expected variable type for ${type.name} operation")
            val name = expr.expr.value
            val scope = expr.expr.index
            var curr = intExpr(expr.expr, "++ Increment")
            if (expr.left) {
                update(scope, name, if (type == INCREMENT) ++curr else --curr)
                curr
            } else {
                update(scope, name, if (type == INCREMENT) curr + 1 else curr - 1)
                curr
            }
        }
        KITA -> {
            var operand: Any = expr.expr
            if (operand !is Expression.ExpressionList)
                operand = unbox(eval(operand as Expression))
            if (operand !is Expression.ExpressionList)
                throw RuntimeException("Expected body operand for kita, but got $operand")
            val evaluated = arrayOfNulls<Any>(operand.size)
            for ((index, aExpr) in operand.expressions.withIndex())
                evaluated[index] = unbox(eval(aExpr))
            evaluated
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
                    is Int, is String, is Char -> if (type == EQUALS) left == right else left != right
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
            val toUpdate = expr.left
            val value = eval(expr.right)
            when (toUpdate) {
                is Expression.Alpha -> update(toUpdate.index, toUpdate.value, value)
                is Expression.ElementAccess -> {
                    val array = unbox(eval(toUpdate.expr))
                    val index = intExpr(toUpdate.index, "[] ArraySet")

                    @Suppress("UNCHECKED_CAST")
                    when (getType(array)) {
                        C_ARRAY -> (array as Array<Any>)[index] = value
                        C_STRING -> {
                            if (value !is Char) throw RuntimeException("string[index] requires a Char")
                            (array as String).replaceRange(index, index, value.toString())
                        }
                        else -> throw RuntimeException("Unknown element access of $array")
                    }
                }
                else -> throw RuntimeException("Unknown left operand for [= Assignment]: $toUpdate")
            }
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

    override fun dot(dot: Expression.Dot): Any {
        when (dot.type) {
            Expression.DotType.FORMAT -> {
                val string = unbox(eval(dot.operand))
                if (getType(string) != C_STRING)
                    throw RuntimeException("String format requires a string operand, but got $string")
                val splits = string
                // TODO:
                //  think of a best way to parse it
            }
        }
        throw RuntimeException("Unknown operation ${dot.type}")
    }

    override fun nativeCall(call: Expression.NativeCall): Any {
        val argsSize = call.arguments.size
        when (val type = call.type) {
            PRINT, PRINTLN -> {
                var printCount = 0
                call.arguments.expressions.forEach {
                    var printable = unbox(eval(it))
                    printable = if (printable is Array<*>) printable.contentDeepToString() else printable.toString()

                    printCount += printable.length
                    print(printable)
                }
                if (type == PRINTLN) print('\n')
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
                return when (val data = unbox(eval(call.arguments.expressions[0]))) {
                    is String -> data.length
                    is Array<*> -> data.size
                    is Expression.ExpressionList -> data.size
                    else -> throw RuntimeException("Unknown measurable data type $data")
                }
            }
            else -> throw RuntimeException("Unknown native read write $type")
        }
    }

    override fun methodCall(call: Expression.MethodCall): Any {
        val fnName = call.name
        val fn = memory.getFn(call.atFrame, call.mIndex, fnName)
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

            val callValue = unbox(eval(callExpressions.next()))
            val gotTypeSignature = getType(callValue)

            if (typeSignature != gotTypeSignature)
                throw RuntimeException("Expected type $typeSignature for arg '${definedParameter.name}' for function $fnName but got $gotTypeSignature")
            callValues.add(Pair(definedParameter, callValue))
        }
        memory.enterScope()
        callValues.forEach {
            val definedParameter = it.first
            val value = it.second
            memory.declareVar(definedParameter.name,
                Entity(definedParameter.name, true, value, definedParameter.type))
        }
        var result = eval(fn.body)
        memory.leaveScope()
        if (result is FlowBlack && result.interrupt == Interrupt.RETURN)
            result = result.data!!

        val returnSignature = fn.returnType
        val gotReturnSignature = getType(result)

        if (returnSignature != C_ANY && returnSignature != gotReturnSignature)
            throw RuntimeException("Expected return type $returnSignature for function $fnName but got $gotReturnSignature")

        return result
    }

    override fun until(until: Expression.Until): Any {
        while (booleanExpr(until.expression, "Until Condition")) {
            memory.enterScope()
            val result = eval(until.body)
            memory.leaveScope()
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
        val iterable = unbox(eval(forEach.entity))

        var index = 0
        val size:Int
        val type: Type

        val getNext: () -> Any
        when (iterable) {
            is String -> {
                size = iterable.length
                getNext = { iterable[index++] }
            }

            is Array<*> -> {
                size = iterable.size
                getNext = { iterable[index++]!! }
            }

            else -> throw RuntimeException("Unknown non-iterable element $iterable")
        }

        val named = forEach.name
        val body = forEach.body

        while (index < size) {
            memory.enterScope()
            val element = getNext()
            memory.declareVar(named, Entity(named, false, element, getType(element)))
            val result = eval(body)
            memory.leaveScope()
            if (result is FlowBlack)
                when (result.interrupt) {
                    Interrupt.BREAK -> break
                    Interrupt.CONTINUE -> continue
                    Interrupt.RETURN -> return result
                    else -> { }
                }
        }
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
            memory.enterScope()
            memory.declareVar(named, Entity(named, false, from, C_INT))
            val result = eval(itr.body)
            memory.leaveScope()
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
        memory.enterScope()
        forLoop.initializer?.let { eval(it) }

        val conditional = forLoop.conditional

        var loopResult: Any = forLoop
        while (if (conditional == null) true else booleanExpr(conditional, "ForLoop")) {
            memory.enterScope()
            val result = eval(forLoop.body)
            memory.leaveScope()
            if (result is FlowBlack) {
                when (result.interrupt) {
                    Interrupt.BREAK -> break
                    Interrupt.CONTINUE -> continue
                    Interrupt.RETURN -> {
                        loopResult = result
                        break
                    }
                    else -> { }
                }
            }
            forLoop.operational?.let { eval(it) }
        }

        memory.leaveScope()
        return loopResult
    }

    override fun interruption(interruption: Expression.Interruption) = when (val type = eval(interruption.type)) {
        RETURN -> FlowBlack(Interrupt.RETURN, eval(interruption.expr!!))
        BREAK -> FlowBlack(Interrupt.BREAK)
        CONTINUE -> FlowBlack(Interrupt.CONTINUE)
        else -> throw RuntimeException("Unknown interruption type $type")
    }

    override fun ifFunction(ifExpr: Expression.If): Any {
        val body = if (booleanExpr(ifExpr.condition, "If Condition")) ifExpr.thenBody else ifExpr.elseBody
        if (body != null) {
            val newScope = body is Expression.ExpressionList
            if (newScope) memory.enterScope()
            val result = eval(body)
            if (newScope) memory.leaveScope()
            return result
        }
        return ifExpr
    }

    override fun function(function: Expression.Function): Any {
        memory.declareFn(function.name, function)
        return function
    }

    override fun elementAccess(access: Expression.ElementAccess): Any {
        val entity = unbox(eval(access.expr))
        val index = intExpr(access.index, "[] ArrayAccess")

        val type = getType(entity)
        return when (type) {
            C_STRING -> (entity as String)[index]
            C_ARRAY -> (entity as Array<*>)[index]!!
            else -> throw RuntimeException("Unknown element access of $entity")
        }
    }
}