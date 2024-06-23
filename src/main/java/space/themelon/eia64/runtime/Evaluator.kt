package space.themelon.eia64.runtime

import space.themelon.eia64.Expression
import space.themelon.eia64.runtime.Entity.Companion.getType
import space.themelon.eia64.runtime.Entity.Companion.unbox
import space.themelon.eia64.syntax.Type
import space.themelon.eia64.syntax.Type.*
import java.util.*
import kotlin.collections.ArrayList

class Evaluator(private val executor: Executor) : Expression.Visitor<Any> {

    fun eval(expr: Expression) = expr.accept(this)
    private fun unboxEval(expr: Expression) = unbox(eval(expr))

    private fun safeUnbox(expr: Expression, expectedType: Type, operation: String): Any {
        val result = eval(expr)
        val gotType = getType(result)
        if (gotType != expectedType)
            throw RuntimeException("Expected type $expectedType for [$operation] but got $gotType")
        return unbox(result)
    }

    private fun booleanExpr(expr: Expression, operation: String) = safeUnbox(expr, E_BOOL, operation) as Boolean
    private fun intExpr(expr: Expression, operation: String) = safeUnbox(expr, E_INT, operation) as Int

    private val memory = Memory()

    override fun literal(literal: Expression.Literal) = literal.data
    override fun alpha(alpha: Expression.Alpha) = memory.getVar(alpha.index, alpha.value)
    override fun operator(operator: Expression.Operator) = operator.value

    private fun define(mutable: Boolean, def: Expression.DefinitionType, value: Any) {
        // make sure variable type = assigned type
        val valueType = getType(value)
        if (def.type != E_ANY && def.type != valueType)
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

            if (getType(left) == E_INT && getType(right) == E_INT)
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
            val value = unboxEval(expr.right)
            when (toUpdate) {
                is Expression.Alpha -> update(toUpdate.index, toUpdate.value, value)
                is Expression.ElementAccess -> {
                    val array = unboxEval(toUpdate.expr)
                    val index = intExpr(toUpdate.index, "[] ArraySet")

                    @Suppress("UNCHECKED_CAST")
                    when (getType(array)) {
                        E_ARRAY -> (array as Array<Any>)[index] = value
                        E_STRING -> {
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
        ADDITIVE_ASSIGNMENT -> {
            TODO("Not yet implemented")
        }
        DEDUCTIVE_ASSIGNMENT -> {
            TODO("Not yet implemented")
        }
        MULTIPLICATIVE_ASSIGNMENT -> {
            TODO("Not yet implemented")
        }
        DIVIDIVE_ASSIGNMENT -> {
            TODO("Not yet implemented")
        }
        BITWISE_AND -> intExpr(expr.left, "& BitwiseAnd") and intExpr(expr.right, "& BitwiseAnd")
        BITWISE_OR -> intExpr(expr.left, "| BitwiseOr") or intExpr(expr.right, "| BitwiseOr")
        else -> throw RuntimeException("Unknown binary operator $type")
    }

    override fun expressions(list: Expression.ExpressionList): Any {
        if (list.preserveState)
            // it is being stored somewhere, like in a variable, etc.
            //   that's why we shouldn't evaluate it
            return list
        for (expression in list.expressions) {
            val result = eval(expression)
            if (result is Entity) {
                // flow interruption is just forwarded
                when (result.type) {
                    RETURN, BREAK, CONTINUE -> return result
                    else -> { }
                }
            }
        }
        return list
    }

    override fun importStdLib(stdLib: Expression.ImportStdLib): Any {
        executor.loadExternal("${Executor.STD_LIB}/${stdLib.name}.eia", stdLib.name)
        return true
    }

    override fun nativeCall(call: Expression.NativeCall): Any {
        val argsSize = call.arguments.size
        when (val type = call.type) {
            PRINT, PRINTLN -> {
                var printCount = 0
                call.arguments.expressions.forEach {
                    var printable = unboxEval(it)
                    printable = if (printable is Array<*>) printable.contentDeepToString() else printable.toString()

                    printCount += printable.length
                    print(printable)
                }
                if (type == PRINTLN) print('\n')
                return printCount
            }

            READ, READLN -> {
                if (argsSize != 0) reportWrongArguments("read/readln", 0, argsSize)
                return Scanner(System.`in`).let { if (type == READ) it.next() else it.nextLine() }
            }

            SLEEP -> {
                if (argsSize != 1) reportWrongArguments("sleep", 1, argsSize)
                val ms = intExpr(call.arguments.expressions[0], "sleep()")
                Thread.sleep(ms.toLong())
                return ms
            }

            LEN -> {
                if (argsSize != 1) reportWrongArguments("len", 1, argsSize)
                return when (val data = unboxEval(call.arguments.expressions[0])) {
                    is String -> data.length
                    is Array<*> -> data.size
                    is Expression.ExpressionList -> data.size
                    else -> throw RuntimeException("Unknown measurable data type $data")
                }
            }

            FORMAT -> {
                val exprs = call.arguments.expressions
                val string = unbox(eval(exprs[0]))
                if (getType(string) != E_STRING)
                    throw RuntimeException("format() requires a string argument")
                string as String
                if (exprs.size > 1) {
                    val values = arrayOfNulls<Any>(exprs.size - 1)
                    for (i in 1 until exprs.size) {
                        values[i - 1] = unbox(eval(exprs[i]))
                    }
                    return String.format(string, *values)
                }
                return string
            }

            INT_CAST -> {
                if (argsSize != 1) reportWrongArguments("int", 1, argsSize)
                val obj = unboxEval(call.arguments.expressions[0])
                if (getType(obj) == E_INT) return obj
                return Integer.parseInt(obj.toString())
            }

            STRING_CAST -> {
                if (argsSize != 1) reportWrongArguments("str", 1, argsSize)
                val obj = unboxEval(call.arguments.expressions[0])
                if (getType(obj) == E_STRING) return obj
                return obj.toString()
            }

            BOOL_CAST -> {
                if (argsSize != 1) reportWrongArguments("bool", 1, argsSize)
                val obj = unboxEval(call.arguments.expressions[0])
                if (getType(obj) == E_INT) return obj
                return if (obj == "true") true else if (obj == "false") false else throw RuntimeException("Cannot parse boolean value: $obj")
            }

            TYPE -> {
                if (argsSize != 1) reportWrongArguments("type", 1, argsSize)
                val obj = unboxEval(call.arguments.expressions[0])
                return getType(obj).toString()
            }

            INCLUDE -> {
                if (argsSize != 1) reportWrongArguments("include", 1, argsSize)
                val obj = unboxEval(call.arguments.expressions[0])
                if (obj !is String)
                    throw RuntimeException("Expected a string argument for include() but got $obj")
                val parts = obj.split(":")
                if (parts.size != 2)
                    throw RuntimeException("include() received invalid argument: $obj")
                var group = parts[0]
                if (group.isEmpty()) group = Executor.STD_LIB

                val name = parts[1]
                executor.loadExternal("$group/$name.eia", name)
                return true
            }
            else -> throw RuntimeException("Unknown native call operation: '$type'")
        }
    }

    override fun methodCall(call: Expression.MethodCall): Any {
        val fnName = call.name
        val fn = memory.getFn(call.atFrame, call.mIndex, fnName)
        return fnInvoke(fn, call.arguments)
    }

    private fun dynamicFnCall(
        name: String,
        args: List<Expression>
    ) = fnInvoke(memory.dynamicFnSearch(name), args)

    override fun classMethodCall(classMethod: Expression.ClassMethodCall): Any {
        val executor = executor.getExternalExecutor(classMethod.className)
            ?: throw RuntimeException("Couldn't find external executor ${classMethod.className}")
        return executor.dynamicFnCall(classMethod.method, classMethod.arguments)
    }

    private fun fnInvoke(fn: Expression.Function, callArgs: List<Expression>): Any {
        val fnName = fn.name

        val sigArgsSize = fn.arguments.size
        val callArgsSize = callArgs.size

        if (sigArgsSize != callArgsSize)
            reportWrongArguments(fnName, sigArgsSize, callArgsSize)
        val parameters = fn.arguments.iterator()
        val callExpressions = callArgs.iterator()

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
        // do not handle return calls here, let it naturally unbox itself
        val result = eval(fn.body)
        memory.leaveScope()

        val returnSignature = fn.returnType
        val gotReturnSignature = getType(result)

        if (returnSignature != E_ANY && returnSignature != gotReturnSignature)
            throw RuntimeException("Expected return type $returnSignature for function $fnName but got $gotReturnSignature")

        return result
    }

    private fun reportWrongArguments(name: String, expectedArgs: Int, gotArgs: Int) {
        throw RuntimeException("Fn [$name()] expected $expectedArgs but got $gotArgs")
    }

    override fun until(until: Expression.Until): Any {
        while (booleanExpr(until.expression, "Until Condition")) {
            memory.enterScope()
            val result = eval(until.body)
            memory.leaveScope()
            if (result is Entity) {
                when (result.type) {
                    BREAK -> break
                    CONTINUE -> continue
                    RETURN -> return result
                    else -> { }
                }
            }
        }
        return until
    }

    override fun forEach(forEach: Expression.ForEach): Any {
        val iterable = unbox(eval(forEach.entity))

        var index = 0
        val size:Int

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
            if (result is Entity) {
                when (result.type) {
                    BREAK -> break
                    CONTINUE -> continue
                    RETURN -> return result
                    else -> { }
                }
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
            memory.declareVar(named, Entity(named, false, from, E_INT))
            val result = eval(itr.body)
            memory.leaveScope()
            if (result is Entity) {
                when (result.type) {
                    BREAK -> break
                    CONTINUE -> {
                        from += by
                        continue
                    }
                    RETURN -> return result
                    else -> { }
                }
            }
            from += by
        }
        return itr
    }

    override fun forLoop(forLoop: Expression.ForLoop): Any {
        val state = memory.getStateCount()
        forLoop.initializer?.let { eval(it) }

        val conditional = forLoop.conditional

        var returnResult: Any = forLoop
        fun evalOperational() = forLoop.operational?.let { eval(it) }

        while (if (conditional == null) true else booleanExpr(conditional, "ForLoop")) {
            memory.enterScope()
            val result = eval(forLoop.body)
            memory.leaveScope()
            if (result is Entity) {
                when (result.type) {
                    BREAK -> break
                    CONTINUE -> {
                        evalOperational()
                        continue
                    }
                    RETURN -> {
                        returnResult = result
                        break
                    }
                    else -> { }
                }
            }
            evalOperational()
        }
        memory.applyStateCount(state)
        return returnResult
    }

    override fun interruption(interruption: Expression.Interruption) = when (val type = eval(interruption.type)) {
        // wrap it as normal entity, this will be naturally unboxed when called unbox()
        RETURN -> {
            val value = eval(interruption.expr!!)
            Entity("FlowReturn", false, value, RETURN)
        }
        BREAK -> Entity("FlowBreak", false, 0, BREAK)
        CONTINUE -> Entity("FlowContinue", false, 0, CONTINUE)
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
            E_STRING -> (entity as String)[index]
            E_ARRAY -> (entity as Array<*>)[index]!!
            else -> throw RuntimeException("Unknown element access of $entity")
        }
    }
}