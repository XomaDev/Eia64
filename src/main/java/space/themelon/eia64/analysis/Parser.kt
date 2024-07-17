package space.themelon.eia64.analysis

import space.themelon.eia64.Config
import space.themelon.eia64.Expression
import space.themelon.eia64.expressions.*
import space.themelon.eia64.expressions.ArrayLiteral
import space.themelon.eia64.runtime.Executor
import space.themelon.eia64.signatures.*
import space.themelon.eia64.syntax.Flag
import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type
import java.io.File

class Parser(private val executor: Executor) {

    private val resolver = ReferenceResolver()

    private lateinit var tokens: List<Token>
    private var index = 0
    private var size = 0

    lateinit var parsed: ExpressionList

    // Variable questions: are we in a scope that is of loops?
    // and so should we allow `continue` and `break` statement?
    // 0 => Not allowed
    // > 0 => Allowed
    private var iterativeScope = 0

    // maintain a list of externally included classes
    private val classes = ArrayList<String>()
    private val staticClasses = ArrayList<String>()

    fun parse(tokens: List<Token>): ExpressionList {
        index = 0
        size = tokens.size
        this.tokens = tokens

        val expressions = ArrayList<Expression>()
        while (!isEOF()) expressions.add(parseNext())
        if (Config.DEBUG) expressions.forEach { println(it) }
        parsed = ExpressionList(expressions)
        return parsed
    }

    // make sure to update canParseNext() when we add stuff here!
    private fun parseNext(): Expression {
        val token = next()
        if (token.flags.isNotEmpty()) {
            when (token.flags[0]) {
                Flag.LOOP -> return loop(token)
                Flag.V_KEYWORD -> return variableDeclaration(token)
                Flag.INTERRUPTION -> return interruption(token)
                else -> {}
            }
        }
        return when (token.type) {
            Type.IF -> ifDeclaration(token)
            Type.FUN -> fnDeclaration()
            Type.SHADO -> shadoDeclaration()
            Type.INCLUDE -> includeStatement()
            Type.NEW -> newStatement(token)
            Type.THROW -> throwStatement(token)
            Type.WHEN -> whenStatement(token)
            //Type.OPEN_SQUARE -> arrayStatement(token)
            else -> {
                back()
                return parseExpr(0)
            }
        }
    }

    private fun canParseNext(): Boolean {
        val token = peek()
        if (token.flags.isNotEmpty())
            token.flags[0].let {
                if (it == Flag.LOOP
                    || it == Flag.V_KEYWORD
                    || it == Flag.INTERRUPTION)
                    return true
            }
        return when (token.type) {
            Type.IF,
            Type.FUN,
            Type.STD,
            Type.INCLUDE,
            Type.NEW,
            Type.THROW,
            Type.SHADO,
            //Type.OPEN_SQUARE,
            Type.WHEN -> true
            else -> false
        }
    }

    private fun throwStatement(token: Token) = ThrowExpr(token, parseNext())

    private fun includeStatement(): Expression {
        expectType(Type.OPEN_CURVE)
        val classNames = mutableListOf<String>()
        while (true) {
            val next = next()
            when (next.type) {
                // means importing only one static instance of the class
                Type.STATIC -> classNames.add(includeStatic())
                Type.STD -> {
                    expectType(Type.COLON)

                    println(peek())
                    val filePath = if (isNext(Type.ALPHA)) readAlpha()
                    else expectType(Type.E_STRING).data as String

                    val file = File("${Executor.STD_LIB}/$filePath.eia")
                    val moduleName = getModuleName(file)
                    classNames.add(moduleName)

                    println(file)
                    classes.add(moduleName)
                    executor.addModule(file.absolutePath, moduleName)
                }
                Type.E_STRING -> {
                    // include("simulationenv/HelloProgram")
                    var path = next.data as String + ".eia"
                    if (path.startsWith("stdlib")) {
                        path = path.replaceFirst("stdlib", Executor.STD_LIB)
                    }
                    val sourceFile = getModulePath(path)

                    verifyFilePath(sourceFile, next)
                    val moduleName = getModuleName(sourceFile)
                    classes.add(moduleName)
                    executor.addModule(sourceFile.absolutePath, moduleName)
                }
                else -> next.error("Unexpected token")
            }
            if (peek().type == Type.CLOSE_CURVE) break
            expectType(Type.COMMA)
        }
        expectType(Type.CLOSE_CURVE)
        return Include(classNames)
    }

    private fun includeStatic(): String {
        expectType(Type.COLON)
        val path = next()
        val sourceFile = if (path.type == Type.STD) {
            expectType(Type.COLON)
            File("${Executor.STD_LIB}/${readAlpha()}.eia")
        } else {
            if (path.type != Type.E_STRING)
                path.error<String>("Expected a string type for static:")
            File("${getModulePath(path.data as String)}.eia")
        }
        verifyFilePath(sourceFile, path)
        val moduleName = getModuleName(sourceFile)
        classes.add(moduleName)
        staticClasses.add(moduleName)
        executor.addModule(sourceFile.absolutePath, moduleName)
        return moduleName
    }

    private fun getModuleName(sourceFile: File) = sourceFile.name.substring(0, sourceFile.name.length - ".eia".length)

    private fun verifyFilePath(sourceFile: File, next: Token) {
        if (!sourceFile.isFile || !sourceFile.exists()) {
            next.error<String>("Cannot find source file '$sourceFile', make sure it is a full valid path")
        }
    }

    private fun getModulePath(path: String) =
        if (path.startsWith('/')) File(path)
        else File("${Executor.EXECUTION_DIRECTORY}/$path")

    private fun newStatement(token: Token): NewObj {
        val module = readAlpha()
        return NewObj(token,
            module,
            callArguments(),
            executor.getModule(module).getFn("init"))
    }

    private fun parseNextInBrace(): Expression {
        // we do it this way, just calling parseNext() would work, but it increases code flow
        // which may make it harder to debug the Parser.

        expectType(Type.OPEN_CURVE)
        val expr = parseNext()
        expectType(Type.CLOSE_CURVE)
        return expr
    }

    private fun whenStatement(where: Token): Expression {
        val expr = parseNextInBrace()

        // Scope: Automatic
        fun readStatement(): Scope {
            expectType(Type.RIGHT_ARROW)
            return autoScopeBody()
        }

        expectType(Type.OPEN_CURLY)
        val matches = ArrayList<Pair<Expression, Scope>>()
        while (true) {
            val p = peek()
            if (p.type == Type.ELSE) break
            if (p.type == Type.CLOSE_CURLY) {
                where.error<String>("Expected else branch for the when statement")
            }
            val match = parseNext()
            matches.add(match to readStatement())
        }
        expectType(Type.ELSE)
        val elseBranch = readStatement()
        expectType(Type.CLOSE_CURLY)

        if (matches.isEmpty()) {
            where.error<String>("When statement cannot be empty")
        }
        return When(where, expr, matches, elseBranch)
    }

    private fun loop(where: Token): Expression {
        when (where.type) {
            Type.UNTIL -> {
                val expr = parseNextInBrace()
                // Scope: Automatic
                iterativeScope++ // this allows for `continue` and `break` statement
                println("start allowing")
                val body = autoBodyExpr()
                println("end allowing")
                iterativeScope--
                return Until(where, expr, body)
            }

            Type.FOR -> {
                // we cannot expose initializers outside the for loop
                resolver.enterScope()
                expectType(Type.OPEN_CURVE)
                val initializer = if (isNext(Type.COMMA)) null else parseNext()
                expectType(Type.COMMA)
                val conditional = if (isNext(Type.COMMA)) null else parseNext()
                expectType(Type.COMMA)
                val operational = if (isNext(Type.CLOSE_CURVE)) null else parseNext()
                expectType(Type.CLOSE_CURVE)
                // double layer scope wrapping
                iterativeScope++
                val body = autoBodyExpr() // Scope: Automatic
                iterativeScope--
                resolver.leaveScope()
                return ForLoop(
                    where,
                    initializer,
                    conditional,
                    operational,
                    body)
            }

            Type.EACH -> {
                expectType(Type.OPEN_CURVE)
                val iName = readAlpha()
                if (isNext(Type.COLON)) {
                    skip()
                    val from = parseNext()
                    expectType(Type.TO)
                    val to = parseNext()

                    var by: Expression? = null
                    if (isNext(Type.BY)) {
                        skip()
                        by = parseNext()
                    }
                    expectType(Type.CLOSE_CURVE)
                    resolver.enterScope()
                    resolver.defineVariable(iName, Sign.INT)
                    // Manual Scopped!
                    iterativeScope++
                    val body = unscoppedBodyExpr()
                    iterativeScope--
                    resolver.leaveScope()
                    return Itr(where, iName, from, to, by, body)
                } else {
                    expectType(Type.IN)
                    val entity = parseNext()
                    expectType(Type.CLOSE_CURVE)

                    val elementSignature = when (val iterableSignature = entity.sig()) {
                        is ArrayExtension -> iterableSignature.elementSignature
                        Sign.STRING -> Sign.CHAR

                        else -> {
                            where.error<String>("Unknown non iterable element for '$iName'")
                            throw RuntimeException()
                        }
                    }

                    resolver.enterScope()
                    resolver.defineVariable(iName, elementSignature)
                    // Manual Scopped!
                    iterativeScope++
                    val body = unscoppedBodyExpr()
                    iterativeScope--
                    resolver.leaveScope()
                    return ForEach(where, iName, entity, body)
                }
            }

            else -> return where.error("Unknown loop type symbol")
        }
    }

    private fun interruption(token: Token): Interruption {
        // checks if `continue and `break` statement are allowed
        if ((token.type == Type.CONTINUE || token.type == Type.BREAK) && iterativeScope == 0) {
            val type = if (token.type == Type.CONTINUE) "Continue" else "Break"
            token.error<String>("$type statement is not allowed here") // End of Execution
            throw RuntimeException()
        }
        return Interruption(
            token,
            token.type,
            when (token.type) {
                Type.RETURN -> parseNext()
                Type.USE -> parseNext()
                else -> null
            }
        )
    }

    private fun fnDeclaration(): Expression {
        val name = readAlpha()

        expectType(Type.OPEN_CURVE)
        val requiredArgs = mutableListOf<Pair<String, Signature>>()
        while (!isEOF() && peek().type != Type.CLOSE_CURVE) {
            val parameterName = readAlpha()
            expectType(Type.COLON)
            val signature = readSignature(next())

            requiredArgs.add(Pair(parameterName, signature))
            if (!isNext(Type.COMMA)) break
            skip()
        }
        expectType(Type.CLOSE_CURVE)

        val returnSignature = if (isNext(Type.COLON)) {
            skip()
            readSignature(next())
        } else Sign.ANY

        // create a wrapper object, that can be set to actual value later
        val reference = FunctionReference(null, requiredArgs, requiredArgs.size, returnSignature)
        resolver.defineFn(name, reference)
        resolver.enterScope()

        requiredArgs.forEach { resolver.defineVariable(it.first, it.second) }

        val body = unitBody() // Fully Manual Scopped
        resolver.leaveScope()
        val fnExpr = FunctionExpr(name, requiredArgs, returnSignature, body)
        reference.fnExpression = fnExpr
        return fnExpr
    }

    private fun shadoDeclaration(): Shadow {
        val names = ArrayList<String>()

        resolver.enterScope()
        expectType(Type.OPEN_CURVE)
        while (!isEOF() && peek().type != Type.CLOSE_CURVE) {
            val name = readAlpha()
            expectType(Type.COLON)

            val argSignature = readSignature(next())
            resolver.defineVariable(name, argSignature)
            names.add(name)
            if (!isNext(Type.COMMA)) break
            skip()
        }
        expectType(Type.CLOSE_CURVE)
        val body = unitBody() // Fully Manual Scopped
        resolver.leaveScope()
        return Shadow(names, body)
    }

    private fun unitBody() = if (isNext(Type.ASSIGNMENT)) {
        skip()
        parseNext()
    } else expressions()

    private fun ifDeclaration(where: Token): Expression {
        val logicalExpr = parseNextInBrace()
        val ifBody = autoBodyExpr()

        // All is Auto Scopped!
        if (isEOF() || peek().type != Type.ELSE) {
            return IfStatement(where, logicalExpr, ifBody)
        }
        skip()

        val elseBranch = when (peek().type) {
            Type.IF -> ifDeclaration(next())
            else -> autoBodyExpr()
        }
        return IfStatement(where, logicalExpr, ifBody, elseBranch)
    }

    private fun autoBodyExpr(): Scope {
        // used everywhere where there is no manual scope management is required,
        //  e.g., IfExpr, Until, For
        if (peek().type == Type.OPEN_CURLY) return autoScopeBody()
        resolver.enterScope()
        return Scope(parseNext(), resolver.leaveScope())
    }

    private fun autoScopeBody(): Scope {
        resolver.enterScope()
        return Scope(expressions(), resolver.leaveScope())
    }

    private fun unscoppedBodyExpr(): Expression {
        if (peek().type == Type.OPEN_CURLY) return expressions()
        return parseNext()
    }

    private fun expressions(): Expression {
        expectType(Type.OPEN_CURLY)
        val expressions = ArrayList<Expression>()
        if (peek().type == Type.CLOSE_CURLY)
            return ExpressionList(expressions)
        while (!isEOF() && peek().type != Type.CLOSE_CURLY)
            expressions.add(parseNext())
        expectType(Type.CLOSE_CURLY)
        if (expressions.size == 1) return expressions[0]
        return ExpressionList(expressions)
    }

    private fun variableDeclaration(where: Token): Expression {
        val name = readAlpha()

        //if (name == "index") {
            //where.error<String>("not here!")
        //}
        val expr: Expression
        val signature: Signature

        if (!isNext(Type.COLON)) {
            // Auto expression type, signature is decided by expression assigned to it
            val assignmentExpr = readVariableExpr()

            signature = assignmentExpr.sig()
            expr = AutoVariable(where, name, assignmentExpr)
        } else {
            skip()
            signature = readSignature(next())

            expr = ExplicitVariable(
                where,
                where.type == Type.VAR,
                name,
                readVariableExpr(),
                signature
            )
        }
        resolver.defineVariable(name, signature)
        return expr
    }

    private fun readSignature(token: Token): Signature {
        if (token.hasFlag(Flag.CLASS)) {
            // then wrap it around Simple Signature
            return when (val classType = token.type) {
                Type.E_INT -> Sign.INT
                Type.E_STRING -> Sign.STRING
                Type.E_CHAR -> Sign.CHAR
                Type.E_BOOL -> Sign.BOOL
                Type.E_ANY -> Sign.ANY
                Type.E_UNIT -> Sign.UNIT
                Type.E_ARRAY -> {
                    val elementSignature: Signature
                    if (isNext(Type.LEFT_DIAMOND)) {
                        skip()
                        elementSignature = readSignature(next())
                        expectType(Type.RIGHT_DIAMOND)
                    } else {
                        elementSignature = Sign.ANY
                    }
                    return ArrayExtension(elementSignature)
                }
                Type.E_OBJECT -> ObjectExtension(Sign.OBJECT.type) // Generic form
                else -> token.error("Unknown class $classType")
            }
        }

        if (token.type != Type.ALPHA) {
            token.error<String>("Expected a class type")
            // end of execution
        }
        // TODO:
        //  in future we need to add an array type extension
        if (classes.contains(token.data as String)) {
            // class that was included from external files
            // this will be an extension of Object class type
            return ObjectExtension(token.data)
        }
        token.error<String>("Unknown class ${token.data}")
        throw RuntimeException()
    }

    private fun readVariableExpr(): Expression {
        val nextToken = peek()
        return when (nextToken.type) {
            Type.ASSIGNMENT -> {
                skip()
                parseNext()
            }

            Type.OPEN_CURVE -> shadoDeclaration()
            Type.OPEN_CURLY -> parseNext()
            else -> nextToken.error("Unexpected variable expression")
        }
    }

    private fun parseExpr(minPrecedence: Int): Expression {
        // this parses a full expressions, until it's done!
        var left = parseElement()
        if (!isEOF() && peek().hasFlag(Flag.POSSIBLE_RIGHT_UNARY)) {
            val where = next()
            left = UnaryOperation(where, where.type, left, false)
        }
        while (!isEOF()) {
            val opToken = peek()
            if (!opToken.hasFlag(Flag.OPERATOR)) return left

            val precedence = operatorPrecedence(opToken.flags[0])
            if (precedence == -1) return left

            if (precedence >= minPrecedence) {
                skip() // operator token
                val right =
                    if (opToken.hasFlag(Flag.PRESERVE_ORDER)) parseTerm()
                    else parseExpr(precedence)
                left = BinaryOperation(
                    opToken,
                    left,
                    right,
                    opToken.type
                )
            } else return left
        }
        return left
    }

    private fun parseElement(): Expression {
        var left = parseTerm()
        // checks for calling methods located in different classes and also
        //  for array access parsing
        while (!isEOF()) {
            val nextOp = peek()
            if (nextOp.type != Type.DOT // (left is class) trying to call a method on an object. e.g. person.sayHello()
                && !(nextOp.type == Type.OPEN_CURVE && !isLiteral(left)) // (left points/is a unit)
                && nextOp.type != Type.OPEN_SQUARE // array element access
                && nextOp.type != Type.CAST // value casting
            ) break

            left = when (nextOp.type) {
                // calling shadow func
                Type.OPEN_CURVE -> unitCall(left)
                Type.OPEN_SQUARE -> {
                    // array access
                    skip()
                    val expr = parseNext()
                    expectType(Type.CLOSE_SQUARE)
                    ArrayAccess(left.marking!!, left, expr)
                }
                Type.CAST -> {
                    skip()
                    Cast(nextOp, left, readSignature(next()))
                }
                else -> classMethodCall(left)
            }
        }
        return left
    }

    private fun isLiteral(expression: Expression) = when (expression) {
        is IntLiteral,
        is StringLiteral, -> true
        is BoolLiteral, -> true
        is CharLiteral, -> true
        is ArrayLiteral, -> true
        else -> false
    }

    private fun classMethodCall(objExpr: Expression): Expression {
        skip()

        val method = next()
        val methodName = readAlpha(method)

        // First Case: Pure static invocation `Person.sayHello()`
        // Second Case: Linked Static Invocation
        //    let myString = " Meow "
        //    println(myString.trim())
        // Third Case: Object Invocation
        //    let myPerson = new Person("Miaw")
        //    println(myPerson.sayHello())

        val module: String
        val signature = objExpr.sig()
        var linked = false
        module = if (objExpr is Alpha && objExpr.index == -2) {
            // Pure static invocation
            objExpr.value
        } else if (signature is SimpleSignature) {
            // Linked Static Invocation
            linked = true
            translateModule(signature, method)
        } else if (signature is ObjectExtension) {
            // Object Invocation
            signature.extensionClass
        } else {
            // TODO: we'll have to work on a fix for this
            println("module is: $signature")
            signature as ArrayExtension
            linked = true
            "array"
        }
        val fnReference = executor.getModule(module).getFn(methodName)
            ?: throw RuntimeException("Could not find function '$methodName' in module _")
        val arguments = callArguments()

        return ClassMethodCall(
            objExpr.marking!!,
            objExpr is Alpha && staticClasses.contains(objExpr.value),
            linked,
            objExpr,
            methodName,
            arguments,
            fnReference,
            module
        )
    }

    private fun translateModule(
        signature: Signature,
        where: Token
    ) = when (signature) {
        Sign.NONE -> where.error("Signature type NONE has no module")
        Sign.ANY -> where.error("Signature type ANY has no module")
        Sign.CHAR -> where.error("Signature type CHAR has no module")
        Sign.UNIT -> where.error("Signature type UNIT has no module")
        Sign.OBJECT -> where.error("Signature type OBJECT has no module") // (Raw Object sign)
        Sign.INT -> "eint"
        Sign.STRING -> "string"
        Sign.BOOL -> "bool"
        Sign.ARRAY -> "array"
        else -> where.error("Unknown object signature $signature")
    }

    private fun getFn(name: String) = resolver.resolveFn(name)

    private fun operatorPrecedence(type: Flag) = when (type) {
        Flag.ASSIGNMENT_TYPE -> 1
        Flag.LOGICAL_OR -> 2
        Flag.LOGICAL_AND -> 3
        Flag.BITWISE_OR -> 4
        Flag.BITWISE_AND -> 5
        Flag.EQUALITY -> 6
        Flag.RELATIONAL -> 7
        Flag.BINARY -> 8
        Flag.BINARY_L2 -> 9
        Flag.BINARY_L3 -> 10
        else -> -1
    }

    private fun parseTerm(): Expression {
        // a term is only one value, like 'a', '123'
        when (peek().type) {
            Type.OPEN_CURVE -> {
                skip()
                val expr = parseNext()
                expectType(Type.CLOSE_CURVE)
                return expr
            }

            Type.OPEN_CURLY -> return Shadow(emptyList(), autoScopeBody().expr)

            else -> {}
        }
        val token = next()
        if (token.hasFlag(Flag.VALUE)) {
            val value = parseValue(token)
            if (!token.hasFlag(Flag.CONSTANT_VALUE) // not a hard constant, like `123` or `"Hello, World"`
                && !isEOF()
                && peek().type == Type.OPEN_CURVE)
                return unitCall(value)
            return value
        } else if (token.hasFlag(Flag.UNARY)) {
            return UnaryOperation(token, token.type, parseTerm(), true)
        } else if (token.hasFlag(Flag.NATIVE_CALL)) {
            val arguments = callArguments()
            return NativeCall(token, token.type, arguments)
        } else if (token.type == Type.ARRAY_OF) {
            // Used to allocate arrays
            // let names = arralloc<String>(5, "default value")
            // println(names[0])
            if (isNext(Type.OPEN_CURVE)) {
                skip()
                return arrayStatement(token)
            } else {
                expectType(Type.LEFT_DIAMOND)
                val elementSignature = readSignature(next())
                expectType(Type.RIGHT_DIAMOND)

                expectType(Type.OPEN_CURVE)
                val size = parseNext()
                expectType(Type.COMMA)
                val defaultValue = parseNext()
                expectType(Type.CLOSE_CURVE)

                return ArrayAllocation(token, elementSignature, size, defaultValue)
            }
        }
        back()
        if (canParseNext()) return parseNext()
        return token.error("Unexpected token")
    }

    private fun arrayStatement(token: Token): ArrayLiteral {
        // an array declared using [ ]
        val arrayElements = mutableListOf<Expression>()
        if (peek().type != Type.CLOSE_CURVE) {
            while (true) {
                arrayElements.add(parseNext())
                val next = next()
                if (next.type == Type.CLOSE_CURVE) break
                else if (next.type != Type.COMMA) next.error<String>("Expected comma for array element separator")
            }
        }
        // Auto array, element-signature is selected based on array content
        return ArrayLiteral(token, arrayElements)
    }

    private fun parseValue(token: Token): Expression {
        return when (token.type) {
            Type.E_TRUE, Type.E_FALSE -> BoolLiteral(token, token.type == Type.E_TRUE)
            Type.E_INT -> IntLiteral(token, token.data.toString().toInt())
            Type.E_STRING -> StringLiteral(token, token.data as String)
            Type.E_CHAR -> CharLiteral(token, token.data as Char)
            Type.ALPHA -> {
                val name = readAlpha(token)
                val vrReference = resolver.resolveVr(name)
                if (vrReference == null) {
                    // could be a function call or static invocation
                    if (resolver.resolveFn(name) != null)
                        Alpha(token, -3, name, Sign.NONE)
                    else if (staticClasses.contains(name))
                        Alpha(token, -2, name, Sign.NONE)
                    else token.error<Expression>("Could not resolve name $name")
                } else {
                    Alpha(token, vrReference.index, name, vrReference.signature)
                }
            }

            Type.OPEN_CURVE -> {
                val expr = parseNext()
                expectType(Type.CLOSE_CURVE)
                expr
            }

            else -> token.error("Unknown token type")
        }
    }

    private fun unitCall(unitExpr: Expression): Expression {
        // only limited to functions or shado variables inside the class
        //  does not touch outside classes
        val arguments = callArguments()

        if (unitExpr is Alpha) {
            val name = unitExpr.value
            val fnExpr = resolver.resolveFn(name)
            if (fnExpr != null) {
                if (fnExpr.argsSize == -1)
                    throw RuntimeException("[Internal] Function args size is not yet set")
                return MethodCall(unitExpr.marking!!, fnExpr, arguments)
            }
        }
        return ShadoInvoke(unitExpr.marking!!, unitExpr, arguments)
    }

    private fun callArguments(): List<Expression> {
        expectType(Type.OPEN_CURVE)
        val arguments = parseArguments()
        expectType(Type.CLOSE_CURVE)
        return arguments
    }

    private fun parseArguments(): List<Expression> {
        val expressions = ArrayList<Expression>()
        if (!isEOF() && peek().type == Type.CLOSE_CURVE)
            return expressions
        while (!isEOF()) {
            expressions.add(parseNext())
            if (!isNext(Type.COMMA)) break
            skip()
        }
        return expressions
    }

    private fun readAlpha(): String {
        val token = next()
        if (token.type != Type.ALPHA) return token.error("Expected alpha token got $token")
        return token.data as String
    }

    private fun readAlpha(token: Token) =
        if (token.type == Type.ALPHA) token.data as String
        else token.error("Was expecting an alpha token")

    private fun expectType(type: Type): Token {
        val next = next()
        if (next.type != type)
            next.error<String>("Expected token type $type but got $next")
        return next
    }

    private fun isNext(type: Type) = peek().type == type

    private fun back() {
        index--
    }

    private fun skip() {
        index++
    }

    private fun next(): Token {
        if (isEOF()) throw RuntimeException("Early EOF")
        return tokens[index++]
    }

    private fun peek(): Token {
        if (isEOF()) throw RuntimeException("Early EOF")
        return tokens[index]
    }

    private fun isEOF() = index == size
}