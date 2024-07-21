package space.themelon.eia64.runtime

import space.themelon.eia64.Expression
import space.themelon.eia64.expressions.*
import space.themelon.eia64.expressions.FunctionExpr
import space.themelon.eia64.primitives.*
import space.themelon.eia64.runtime.Entity.Companion.getType
import space.themelon.eia64.runtime.Entity.Companion.unbox
import space.themelon.eia64.signatures.ObjectExtension
import space.themelon.eia64.signatures.Sign.intoType
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Type.*
import java.util.Scanner
import kotlin.collections.ArrayList
import kotlin.math.pow
import kotlin.random.Random
import kotlin.system.exitProcess

class Evaluator(
    private val className: String,
    private val executor: Executor
) : Expression.Visitor<Any> {

    private val startupTime = System.currentTimeMillis()

    private var evaluator: Expression.Visitor<Any> = this

    fun shutdown() {
        // Reroute all the traffic to Void, which would raise ShutdownException.
        // We use this strategy to cause an efficient shutdown than checking fields each time
        evaluator = VoidEvaluator()
    }

    fun eval(expr: Expression): Any {
        return expr.accept(evaluator)
    }

    private fun unboxEval(expr: Expression) = unbox(eval(expr))

    private fun booleanExpr(expr: Expression) = unboxEval(expr) as EBool

    private fun intExpr(expr: Expression) = when (val result = unboxEval(expr)) {
        is EChar -> EInt(result.get().code)
        else -> result as EInt
    }

    private val memory = Memory()

    fun clearMemory() {
        memory.clearMemory()
    }

    override fun intLiteral(literal: IntLiteral) = EInt(literal.value)
    override fun boolLiteral(literal: BoolLiteral) = EBool(literal.value)
    override fun stringLiteral(literal: StringLiteral) = EString(literal.value)
    override fun charLiteral(literal: CharLiteral) = EChar(literal.value)

    override fun alpha(alpha: Alpha) = memory.getVar(alpha.index, alpha.value)

    private fun prepareArrayOf(arguments: List<Expression>): EArray {
        val evaluated = arrayOfNulls<Any>(arguments.size)
        for ((index, aExpr) in arguments.withIndex())
            evaluated[index] = unboxEval(aExpr)
        evaluated as Array<Any>
        return EArray(evaluated)
    }

    override fun array(literal: ArrayLiteral) = prepareArrayOf(literal.elements)

    override fun arrayCreation(arrayCreation: StrictArrayCreation) = prepareArrayOf(arrayCreation.elements)

    override fun arrayAllocation(arrayAllocation: ArrayAllocation): Any {
        val size = intExpr(arrayAllocation.size)
        return EArray(Array(size.get()) { unboxEval(arrayAllocation.defaultValue) })
    }

    private fun update(scope: Int, name: String, value: Any) {
        (memory.getVar(scope, name) as Entity).update(value)
    }

    override fun variable(variable: ExplicitVariable): Any {
        val name = variable.name
        val signature = variable.explicitSignature.intoType()
        val value = unboxEval(variable.expr)
        val mutable = variable.mutable

        memory.declareVar(name, Entity(name, mutable, value, signature))
        return value
    }

    override fun autoVariable(autoVariable: AutoVariable): Any {
        val value = unboxEval(autoVariable.expr)
        memory.declareVar(
            autoVariable.name,
            Entity(
                autoVariable.name,
                true,
                unbox(value),
                getType(value)
            )
        )
        return value
    }

    override fun unaryOperation(expr: UnaryOperation): Any = when (val type = expr.operator) {
        NOT -> EBool(!(booleanExpr(expr.expr).get()))
        NEGATE -> EInt(Math.negateExact(intExpr(expr.expr).get()))
        INCREMENT, DECREMENT -> {
            val eInt = intExpr(expr.expr)
            EInt(if (expr.towardsLeft) {
                if (type == INCREMENT) eInt.incrementAndGet()
                else eInt.decrementAndGet()
            } else {
                if (type == INCREMENT) eInt.getAndIncrement()
                else eInt.getAndDecrement()
            })
        }
        else -> throw RuntimeException("Unknown unary operator $type")
    }

    private fun valueEquals(left: Any, right: Any): Boolean {
        if (getType(left) != getType(right)) return false
        when (left) {
            is EInt,
            is EString,
            is EChar,
            is EBool,
            is EArray -> return left == right
        }
        return false
    }

    override fun binaryOperation(expr: BinaryOperation) = when (val type = expr.operator) {
        PLUS -> {
            val left = unboxEval(expr.left)
            val right = unboxEval(expr.right)

            if (getType(left) == E_INT && getType(right) == E_INT)
                left as EInt + right as EInt
            else EString(left.toString() + right.toString())
        }
        NEGATE -> intExpr(expr.left) - intExpr(expr.right)
        TIMES -> intExpr(expr.left) * intExpr(expr.right)
        SLASH -> intExpr(expr.left) / intExpr(expr.right)
        EQUALS, NOT_EQUALS -> {
            val left = unboxEval(expr.left)
            val right = unboxEval(expr.right)

            //println("eq test left $left")
            //println("eq test right $right")
            EBool(if (type == EQUALS) valueEquals(left, right) else !valueEquals(left, right))
        }
        LOGICAL_AND -> booleanExpr(expr.left).and(booleanExpr(expr.right))
        LOGICAL_OR -> booleanExpr(expr.left).or(booleanExpr(expr.right))
        RIGHT_DIAMOND -> EBool(intExpr(expr.left) > intExpr(expr.right))
        LEFT_DIAMOND -> {
            val left = intExpr(expr.left)
            val right = intExpr(expr.right)
            EBool(left < right)
        }
        GREATER_THAN_EQUALS -> EBool(intExpr(expr.left) >= intExpr(expr.right))
        LESSER_THAN_EQUALS -> EBool(intExpr(expr.left) <= intExpr(expr.right))
        ASSIGNMENT -> {
            val toUpdate = expr.left
            val value = unboxEval(expr.right)
            when (toUpdate) {
                is Alpha -> update(toUpdate.index, toUpdate.value, value)
                is ArrayAccess -> {
                    val array = unboxEval(toUpdate.expr)
                    val index = intExpr(toUpdate.index).get()

                    @Suppress("UNCHECKED_CAST")
                    when (getType(array)) {
                        E_ARRAY -> (array as ArrayOperable<Any>).setAt(index, value)
                        E_STRING -> {
                            if (value !is EChar) throw RuntimeException("string[index] requires a Char")
                            (array as EString).setAt(index, value)
                        }
                        else -> throw RuntimeException("Unknown element access of {$array}")
                    }
                }
                else -> throw RuntimeException("Unknown left operand for [= Assignment]: $toUpdate")
            }
            value
        }
        ADDITIVE_ASSIGNMENT -> {
            val element = unboxEval(expr.left)
            when (element) {
                is EString -> element.append(unboxEval(expr.right))
                is EInt -> element += intExpr(expr.right)
                else -> throw RuntimeException("Cannot apply += operator on element $element")
            }
            element
        }
        DEDUCTIVE_ASSIGNMENT -> {
            val element = unboxEval(expr.left)
            when (element) {
                is EInt -> element -= intExpr(expr.right)
                else -> throw RuntimeException("Cannot apply -= operator on element $element")
            }
            element
        }
        MULTIPLICATIVE_ASSIGNMENT -> {
            val element = unboxEval(expr.left)
            when (element) {
                is EInt -> element *= intExpr(expr.right)
                else -> throw RuntimeException("Cannot apply *= operator on element $element")
            }
            element
        }
        POWER -> {
            val left = intExpr(expr.left)
            val right = intExpr(expr.right)
            EString(left.get().toDouble().pow(right.get().toDouble()).toString())
        }
        DIVIDIVE_ASSIGNMENT -> {
            val element = unboxEval(expr.left)
            when (element) {
                is EInt -> element /= intExpr(expr.right)
                else -> throw RuntimeException("Cannot apply /= operator on element $element")
            }
            element
        }
        BITWISE_AND -> intExpr(expr.left).and(intExpr(expr.right))
        BITWISE_OR -> intExpr(expr.left).or(intExpr(expr.right))
        else -> throw RuntimeException("Unknown binary operator $type")
    }

    override fun expressions(list: ExpressionList): Any {
        if (list.preserveState)
            // it is being stored somewhere, like in a variable, etc.
            //   that's why we shouldn't evaluate it
            return list
        var result: Any? = null
        for (expression in list.expressions) {
            result = eval(expression)
            if (result is Entity) {
                // flow interruption is just forwarded
                when (result.type) {
                    RETURN, BREAK, CONTINUE, USE -> return result
                    else -> { }
                }
            }
        }
        return result!!
    }

    override fun include(include: Include): Any {
        include.names.forEach { executor.executeModule(it) }
        return EBool(true)
    }

    override fun new(new: NewObj): Evaluator {
        val evaluator = executor.newEvaluator(new.name)
        evaluator.dynamicFnCall("init", evaluateArgs(new.arguments), true)
        return evaluator
    }

    // try to call a string() method located in local class if available
    @Override
    override fun toString(): String {
        val result = dynamicFnCall(
            "string",
            emptyArray(),
            true,
            "Class<$className>")
        if (result is String) {
            return result
        }
        if (result is EString) {
            return result.get()
        }
        throw RuntimeException("string() returned a non string $result")
    }

    override fun cast(cast: Cast): Any {
        val result = unboxEval(cast.expr)
        val castInto = cast.expectSignature

        if (castInto is ObjectExtension) {
            if (result !is Evaluator) {
                cast.where.error<String>("${getType(result)} cannot be cast into class ${castInto.extensionClass}")
                throw RuntimeException()
            }
            val castClass = castInto.extensionClass
            val gotClass = result.className

            if (castClass != gotClass) {
                cast.where.error<String>("Class $gotClass cannot be cast into $castClass")
                throw RuntimeException()
            }
        }

        val castType = castInto.intoType()
        val gotType = getType(result)
        if (castType != gotType) {
            cast.where.error<String>("Type $gotType cannot be cast into $castType")
            throw RuntimeException()
        }
        return result
    }

    override fun nativeCall(call: NativeCall): Any {
        val argsSize = call.arguments.size
        when (val type = call.call) {
            PRINT, PRINTLN -> {
                var printCount = 0
                call.arguments.forEach {
                    var printable = unboxEval(it)
                    printable = if (printable is Array<*>) printable.contentDeepToString() else printable.toString()

                    printCount += printable.length
                    executor.standardOutput.print(printable)
                }
                if (type == PRINTLN) executor.standardOutput.print('\n')
                return EInt(printCount)
            }

            READ, READLN -> {
                if (argsSize != 0) reportWrongArguments("read/readln", 0, argsSize)
                return EString(Scanner(executor.standardInput).let { if (type == READ) it.next() else it.nextLine() })
            }

            SLEEP -> {
                if (argsSize != 1) reportWrongArguments("sleep", 1, argsSize)
                val millis = intExpr(call.arguments[0])
                Thread.sleep(millis.get().toLong())
                return millis
            }

            LEN -> {
                if (argsSize != 1) reportWrongArguments("len", 1, argsSize)
                return EInt(when (val data = unboxEval(call.arguments[0])) {
                    is EString -> data.length
                    is EArray -> data.size
                    is ExpressionList -> data.size
                    else -> throw RuntimeException("Unknown measurable data type $data")
                })
            }

            FORMAT -> {
                val exprs = call.arguments
                val string = unboxEval(exprs[0])
                if (getType(string) != E_STRING)
                    throw RuntimeException("format() requires a string argument")
                string as EString
                if (exprs.size > 1) {
                    val values = arrayOfNulls<Any>(exprs.size - 1)
                    for (i in 1 until exprs.size) {
                        val value = unboxEval(exprs[i])
                        values[i - 1] = if (value is Primitive<*>) value.get() else value
                    }
                    return EString(String.format(string.get(), *values))
                }
                return string
            }

            INT_CAST -> {
                if (argsSize != 1) reportWrongArguments("int", 1, argsSize)
                val obj = unboxEval(call.arguments[0])

                return when (val objType = getType(obj)) {
                    E_INT -> obj
                    E_CHAR -> EInt((obj as EChar).get().code)
                    E_STRING -> EInt(Integer.parseInt(obj.toString()))
                    else -> throw RuntimeException("Unknown type for int() cast $objType")
                }
            }

            CHAR_CAST -> {
                if (argsSize != 1) reportWrongArguments("char", 1, argsSize)
                val obj = unboxEval(call.arguments[0])
                return when (val objType = getType(obj)) {
                    E_CHAR -> objType
                    E_INT -> EChar((obj as EInt).get().toChar())
                    else -> throw RuntimeException("Unknown type for char() cast $objType")
                }
            }

            STRING_CAST -> {
                if (argsSize != 1) reportWrongArguments("str", 1, argsSize)
                val obj = unboxEval(call.arguments[0])
                if (getType(obj) == E_STRING) return obj
                return EString(obj.toString())
            }

            BOOL_CAST -> {
                if (argsSize != 1) reportWrongArguments("bool", 1, argsSize)
                val obj = unboxEval(call.arguments[0])
                if (getType(obj) == E_BOOL) return obj
                return EBool(when (obj) {
                    "true" -> true
                    "false" -> false
                    else -> throw RuntimeException("Cannot parse boolean value: $obj")
                })
            }

            TYPE -> {
                if (argsSize != 1) reportWrongArguments("type", 1, argsSize)
                val obj = unboxEval(call.arguments[0])
                return EString(getType(obj).toString())
            }

            INCLUDE -> {
                if (argsSize != 1) reportWrongArguments("include", 1, argsSize)
                val obj = unboxEval(call.arguments[0])
                if (obj !is EString)
                    throw RuntimeException("Expected a string argument for include() but got $obj")
                val parts = obj.get().split(":")
                if (parts.size != 2)
                    throw RuntimeException("include() received invalid argument: $obj")
                var group = parts[0]
                if (group.isEmpty()) group = Executor.STD_LIB

                val name = parts[1]
                executor.addModule("$group/$name.eia", name)
                return EBool(true)
            }

            COPY -> {
                if (argsSize != 1) reportWrongArguments("include", 1, argsSize)
                val obj = unboxEval(call.arguments[0])
                if (obj !is Primitive<*> || !obj.isCopyable())
                    throw RuntimeException("Cannot apply copy() on object type ${getType(obj)} = $obj")
                return obj.copy()!!
            }

            //ARRALLOC -> {
                //if (argsSize != 1) reportWrongArguments("arralloc", 1, argsSize)
                //val size = intExpr(call.arguments[0])
                //return EArray(Array(size.get()) { EInt(0) })
            //}

            // Deprecated, we've switched over to array initializers [ ]
            // ARRAYOF -> return prepareArrayOf(call.arguments)

            TIME -> return EInt((System.currentTimeMillis() - startupTime).toInt())

            RAND -> {
                if (argsSize != 2) reportWrongArguments("rand", 2, argsSize)
                val from = intExpr(call.arguments[0])
                val to = intExpr(call.arguments[1])
                return EInt(Random.nextInt(from.get(), to.get()))
            }

            EXIT -> {
                if (argsSize != 1) reportWrongArguments("exit", 1, argsSize)
                val exitCode = intExpr(call.arguments[0])
                exitProcess(exitCode.get())
            }

            MEM_CLEAR -> {
                // for clearing memory of the current class
                memory.clearMemory()
                return EBool(true)
            }
            else -> throw RuntimeException("Unknown native call operation: '$type'")
        }
    }

    override fun throwExpr(throwExpr: ThrowExpr): Any {
        throwExpr.where.error<String>(unboxEval(throwExpr.error).toString())
        // End of Execution
        throw RuntimeException()
    }

    override fun scope(scope: Scope): Any {
        if (scope.imaginary) return eval(scope.expr)
        memory.enterScope()
        val result = eval(scope.expr)
        memory.leaveScope()
        return result
    }

    override fun classPropertyAccess(propertyAccess: ClassPropertyAccess): Any {
        val property = propertyAccess.property
        val moduleName = propertyAccess.moduleInfo.name

        var evaluator: Evaluator? = null
        if (propertyAccess.static) {
            evaluator = executor.getEvaluator(moduleName)
        } else {
            when (val evaluatedObject = unboxEval(propertyAccess.objectExpression)) {
                is Evaluator -> evaluator = evaluatedObject
                is Primitive<*> -> executor.getEvaluator(moduleName)
                else -> throw RuntimeException("Could not find property $property of object $evaluatedObject")
            }
        }
        if (evaluator == null) {
            throw RuntimeException("Could not find module $moduleName")
        }
        val uniqueVariable = propertyAccess.uniqueVariable
        return evaluator.memory.getVar(
            uniqueVariable.index,
            property
        )
    }

    override fun methodCall(call: MethodCall)
        = fnInvoke(call.reference.fnExpression!!, evaluateArgs(call.arguments))

    override fun classMethodCall(call: ClassMethodCall): Any {
        val obj = call.objectExpression
        val methodName = call.method
        val args: Array<Any>

        var evaluator: Evaluator? = null
        // we may need to do a recursive alpha parse
        if (call.static) {
            // static invocation of an included class
            args = evaluateArgs(call.arguments)
        } else {
            val evaluatedObj = unboxEval(obj)
            call.arguments as ArrayList
            args = when (evaluatedObj) {
                is Primitive<*> -> {
                    val evaluatedArgs = arrayOfNulls<Any>(call.arguments.size + 1)
                    for ((index, expression) in call.arguments.withIndex())
                        evaluatedArgs[index + 1] = unboxEval(expression)
                    // NOTE: we never should directly modify the original expression list
                    evaluatedArgs[0] = evaluatedObj
                    @Suppress("UNCHECKED_CAST")
                    evaluatedArgs as Array<Any>
                    evaluatedArgs
                }
                is Evaluator -> {
                    evaluator = evaluatedObj
                    evaluateArgs(call.arguments)
                }
                else -> throw RuntimeException("Could not find method '$methodName' of object $evaluatedObj")
            }
        }
        val moduleName = call.moduleInfo.name
        val finalEvaluator = evaluator ?: executor.getEvaluator(moduleName)
            ?: throw RuntimeException("Could not find module $moduleName")
        return finalEvaluator.fnInvoke(call.reference.fnExpression!!, args)
    }

    private fun evaluateArgs(args: List<Expression>): Array<Any> {
        val evaluatedArgs = arrayOfNulls<Any>(args.size)
        for ((index, expression) in args.withIndex())
            evaluatedArgs[index] = unboxEval(expression)
        @Suppress("UNCHECKED_CAST")
        evaluatedArgs as Array<Any>
        return evaluatedArgs
    }

    private fun dynamicFnCall(
        name: String,
        args: Array<Any>,
        discardIfNotFound: Boolean,
        defaultValue: Any? = null
    ): Any? {
        val fn = memory.dynamicFnSearch(name)
        if (discardIfNotFound && fn == null) return defaultValue
        if (fn == null) throw RuntimeException("Unable to find function '$name()' in class $className")
        return fnInvoke(fn, args)
    }

    private fun fnInvoke(fn: FunctionExpr, callArgs: Array<Any>): Any {
        // Fully Manual Scopped!
        val fnName = fn.name

        val sigArgsSize = fn.arguments.size
        val callArgsSize = callArgs.size

        if (sigArgsSize != callArgsSize)
            reportWrongArguments(fnName, sigArgsSize, callArgsSize)
        val parameters = fn.arguments.iterator()
        val callExpressions = callArgs.iterator()

        // Pair<Pair<Parameter Name, Signature>, Call Value>
        val callValues = ArrayList<Pair<Pair<String, Signature>, Any>>()
        while (parameters.hasNext()) {
            val definedParameter = parameters.next()
            val callValue = callExpressions.next()

            callValues.add(Pair(definedParameter, callValue))
        }
        memory.enterScope()
        callValues.forEach {
            val definedParameter = it.first
            val value = it.second
            memory.declareVar(definedParameter.first,
                Entity(definedParameter.first, true, value, definedParameter.second.intoType()))
        }
        val result = unboxEval(fn.body)
        memory.leaveScope()
        if (fn.isVoid) return fn
        return result
    }

    override fun unitInvoke(shadoInvoke: ShadoInvoke): Any {
        var operand: Any = shadoInvoke.expr

        // Fully Manual Scopped
        if (operand !is Shadow)
            operand = unboxEval(operand as Expression)

        if (operand !is Shadow)
            throw RuntimeException("Expected shadow element for call, but got $operand")

        val expectedArgs = operand.names.size
        val gotArgs = shadoInvoke.arguments.size

        if (expectedArgs != gotArgs) {
            reportWrongArguments("AnonShado", expectedArgs, gotArgs, "Shado")
        }

        val argIterator = operand.names.iterator()
        val exprIterator = evaluateArgs(shadoInvoke.arguments).iterator()

        memory.enterScope()
        while (exprIterator.hasNext()) {
            memory.declareVar(argIterator.next(), exprIterator.next())
        }

        val result = eval(operand.body)
        memory.leaveScope()

        if (result is Entity) {
            when (result.type) {
                RETURN, USE -> return result
                else -> { }
            }
        }
        return result
    }

    private fun reportWrongArguments(name: String, expectedArgs: Int, gotArgs: Int, type: String = "Fn") {
        throw RuntimeException("$type [$name()] expected $expectedArgs but got $gotArgs")
    }

    override fun until(until: Until): Any {
        // Auto Scopped
        var numIterations = 0
        while (booleanExpr(until.expression).get()) {
            numIterations++
            val result = eval(until.body)
            if (result is Entity) {
                when (result.type) {
                    BREAK -> break
                    CONTINUE -> continue
                    RETURN -> return result
                    USE -> result.value
                    else -> { }
                }
            }
        }
        return EInt(numIterations)
    }

    override fun forEach(forEach: ForEach): Any {
        val iterable = unboxEval(forEach.entity)

        var index = 0
        val size:Int

        val getNext: () -> Any
        when (iterable) {
            is EString -> {
                size = iterable.length
                getNext = { iterable.getAt(index++) }
            }

            is EArray -> {
                size = iterable.size
                getNext = { iterable.getAt(index++) }
            }

            else -> throw RuntimeException("Unknown non-iterable element $iterable")
        }

        val named = forEach.name
        val body = forEach.body

        var numIterations = 0
        while (index < size) {
            numIterations++
            // Manual Scopped
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
                    USE -> return result.value
                    else -> { }
                }
            }
        }
        return EInt(numIterations)
    }

    override fun itr(itr: Itr): Any {
        val named = itr.name
        var from = intExpr(itr.from)
        val to = intExpr(itr.to)
        val by = if (itr.by == null) EInt(1) else intExpr(itr.by)

        val reverse = from > to
        if (reverse) by.set(-by.get())

        var numIterations = 0
        while (if (reverse) from >= to else from <= to) {
            numIterations++
            // Manual Scopped
            memory.enterScope()
            memory.declareVar(named, Entity(named, true, from, E_INT))
            val result = eval(itr.body)
            memory.leaveScope()
            if (result is Entity) {
                when (result.type) {
                    BREAK -> break
                    CONTINUE -> {
                        from = from + by
                        continue
                    }
                    RETURN -> return result
                    USE -> return result.value
                    else -> { }
                }
            }
            from = from + by
        }
        return EInt(numIterations)
    }

    override fun forLoop(forLoop: ForLoop): Any {
        memory.enterScope()
        forLoop.initializer?.let { eval(it) }

        val conditional = forLoop.conditional

        var numIterations = 0
        fun evalOperational() = forLoop.operational?.let { eval(it) }

        while (if (conditional == null) true else booleanExpr(conditional).get()) {
            numIterations++
            // Auto Scopped
            val result = eval(forLoop.body)
            if (result is Entity) {
                when (result.type) {
                    BREAK -> break
                    CONTINUE -> {
                        evalOperational()
                        continue
                    }
                    RETURN -> {
                        memory.leaveScope()
                        return result
                    }
                    USE -> {
                        memory.leaveScope()
                        return result.value
                    }
                    else -> { }
                }
            }
            evalOperational()
        }
        memory.leaveScope()
        return EInt(numIterations)
    }

    override fun interruption(interruption: Interruption) = when (val type = interruption.operator) {
        // wrap it as a normal entity, this will be naturally unboxed when called unbox()
        RETURN -> {
            // could be of a void type, so it could be null
            val expr = if (interruption.expr == null) 0
                else unboxEval(interruption.expr)
            Entity("FlowReturn", false, expr, RETURN)
        }
        USE -> Entity("FlowUse", false, unboxEval(interruption.expr!!), USE)
        BREAK -> Entity("FlowBreak", false, 0, BREAK)
        CONTINUE -> Entity("FlowContinue", false, 0, CONTINUE)
        else -> throw RuntimeException("Unknown interruption type $type")
    }

    override fun whenExpr(whenExpr: When): Any {
        // Fully Auto Scopped
        val matchExpr = unboxEval(whenExpr.expr)
        for (match in whenExpr.matches)
            if (valueEquals(matchExpr, unboxEval(match.first)))
                return unboxEval(match.second)
        return unboxEval(whenExpr.defaultBranch)
    }

    override fun ifFunction(ifExpr: IfStatement): Any {
        val conditionSuccess = booleanExpr(ifExpr.condition).get()
        val body = if (conditionSuccess) ifExpr.thenBody else ifExpr.elseBody
        // Auto Scopped
        if (body != null) return eval(body)
        return EBool(conditionSuccess)
    }

    override fun function(function: FunctionExpr): Any {
        memory.declareFn(function.name, function)
        return EBool(true)
    }

    override fun shado(shadow: Shadow) = shadow

    override fun arrayAccess(access: ArrayAccess): Any {
        val entity = unboxEval(access.expr)
        val index = intExpr(access.index).get()

        if (entity !is ArrayOperable<*>)
            throw RuntimeException("Unknown non-array operable element access of $entity")
        return entity.getAt(index)!!
    }
}