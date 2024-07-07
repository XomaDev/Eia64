package space.themelon.eia64.analysis

import space.themelon.eia64.Config
import space.themelon.eia64.Expression
import space.themelon.eia64.runtime.Executor
import space.themelon.eia64.syntax.Flag
import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type
import java.io.File

class Parser(private val executor: Executor) {

    private val resolver = ReferenceResolver()

    private lateinit var tokens: List<Token>
    private var index = 0
    private var size = 0

    lateinit var parsed: Expression.ExpressionList

    fun parse(tokens: List<Token>): Expression.ExpressionList {
        index = 0
        size = tokens.size
        this.tokens = tokens

        val expressions = ArrayList<Expression>()
        while (!isEOF()) expressions.add(parseNext())
        if (Config.DEBUG) expressions.forEach { println(it) }
        parsed = Expression.ExpressionList(expressions)
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
            Type.THROW -> throwStatement()
            Type.WHEN -> whenStatement(token)
            Type.OPEN_SQUARE -> arrayStatement(token)
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
            Type.OPEN_SQUARE,
            Type.WHEN -> true
            else -> false
        }
    }

    private fun throwStatement() = Expression.ThrowExpr(parseNext())

    private fun includeStatement(): Expression {
        expectType(Type.OPEN_CURVE)
        val staticClasses = mutableListOf<String>()
        while (true) {
            val next = next()
            when (next.type) {
                // means importing only one static instance of the class
                Type.STATIC -> staticClasses.add(includeStatic())
                Type.STD -> {
                    expectType(Type.COLON)
                    val file = File("${Executor.STD_LIB}/${readAlpha()}.eia")
                    val moduleName = getModuleName(file)
                    staticClasses.add(moduleName)
                    resolver.classes.add(moduleName)
                    executor.addModule(file.absolutePath, moduleName)
                }
                Type.E_STRING -> {
                    val sourceFile = getModulePath(next.optionalData as String + ".eia")
                    verifyFilePath(sourceFile, next)
                    val moduleName = getModuleName(sourceFile)
                    executor.addModule(sourceFile.absolutePath, moduleName)
                }
                else -> next.error("Unexpected token")
            }
            if (peek().type == Type.CLOSE_CURVE) break
            expectType(Type.COMMA)
        }
        expectType(Type.CLOSE_CURVE)
        return Expression.Include(staticClasses)
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
            File("${getModulePath(path.optionalData as String)}.eia")
        }
        verifyFilePath(sourceFile, path)
        val moduleName = getModuleName(sourceFile)
        resolver.classes.add(moduleName)
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

    private fun newStatement(token: Token) = Expression.NewObj(token, readAlpha(), callArguments())

    private fun arrayStatement(token: Token): Expression.Array {
        val arrayElements = mutableListOf<Expression>()
        if (peek().type != Type.CLOSE_SQUARE) {
            while (true) {
                arrayElements.add(parseNext())
                val next = next()
                if (next.type == Type.CLOSE_SQUARE) break
                else if (next.type != Type.COMMA) next.error<String>("Expected comma for array element separator")
            }
        }
        return Expression.Array(token, arrayElements)
    }

    private fun whenStatement(where: Token): Expression {
        expectType(Type.OPEN_CURVE)
        val expr = parseNext()
        expectType(Type.CLOSE_CURVE)

        // Scope: Automatic
        fun readStatement(): Expression.Scope {
            expectType(Type.RIGHT_ARROW)
            return autoScopeBody()
        }

        expectType(Type.OPEN_CURLY)
        val matches = ArrayList<Pair<Expression, Expression.Scope>>()
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
        return Expression.When(where, expr, matches, elseBranch)
    }

    private fun loop(where: Token): Expression {
        when (where.type) {
            Type.UNTIL -> {
                expectType(Type.OPEN_CURVE)
                val expr = parseNext()
                expectType(Type.CLOSE_CURVE)
                // Scope: Automatic
                return Expression.Until(where, expr, autoBodyExpr())
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
                val body = autoBodyExpr() // Scope: Automatic
                resolver.leaveScope()
                return Expression.ForLoop(
                    where,
                    initializer,
                    conditional,
                    operational,
                    body)
            }

            Type.ITR -> {
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
                    resolver.defineVariable(iName, ExprType.INT)
                    // Manual Scopped!
                    val body = unscoppedBodyExpr()
                    resolver.leaveScope()
                    return Expression.Itr(where, iName, from, to, by, body)
                } else {
                    expectType(Type.IN)
                    val entity = parseNext()
                    expectType(Type.CLOSE_CURVE)
                    resolver.enterScope()
                    // TODO:
                    //  we will have to take a look into this later
                    resolver.defineVariable(iName, ExprType.ANY)
                    // Manual Scopped!
                    val body = unscoppedBodyExpr()
                    resolver.leaveScope()
                    return Expression.ForEach(where, iName, entity, body)
                }
            }

            else -> return where.error("Unknown loop type symbol")
        }
    }

    private fun interruption(token: Token) = Expression.Interruption(
        token,
        token.type,
        when (token.type) {
            Type.RETURN -> parseNext()
            Type.USE -> parseNext()
            else -> null
        }
    )

    private fun fnDeclaration(): Expression {
        val name = readAlpha()

        // create a wrapper object, that can be set to actual value later
        val reference = FunctionReference()
        resolver.defineFn(name, reference)
        resolver.enterScope()

        expectType(Type.OPEN_CURVE)
        val requiredArgs = ArrayList<Expression.DefinitionType>()
        while (!isEOF() && peek().type != Type.CLOSE_CURVE) {
            val parameterName = readAlpha()
            expectType(Type.COLON)
            val clazz = readClassType()
            resolver.defineVariable(parameterName, ExprType.translate(clazz))

            requiredArgs.add(Expression.DefinitionType(parameterName, clazz))
            if (!isNext(Type.COMMA)) break
            skip()
        }
        reference.argsSize = requiredArgs.size
        expectType(Type.CLOSE_CURVE)
        val returnType = if (isNext(Type.COLON)) {
            skip()
            readClassType()
        } else Type.E_ANY

        val body = unitBody() // Fully Manual Scopped
        resolver.leaveScope()
        val fnExpr = Expression.Function(name, requiredArgs, returnType, body)
        reference.fnExpression = fnExpr
        return fnExpr
    }

    private fun shadoDeclaration(): Expression.Shadow {
        val names = ArrayList<String>()

        resolver.enterScope()
        expectType(Type.OPEN_CURVE)
        while (!isEOF() && peek().type != Type.CLOSE_CURVE) {
            val name = readAlpha()
            // TODO: take a look at this later, look into how it can be improved
            resolver.defineVariable(name, ExprType.ANY)
            names.add(name)
            if (!isNext(Type.COMMA)) break
            skip()
        }
        expectType(Type.CLOSE_CURVE)
        val body = unitBody() // Fully Manual Scopped
        resolver.leaveScope()
        return Expression.Shadow(names, body)
    }

    private fun unitBody() = if (isNext(Type.ASSIGNMENT)) {
        skip()
        parseNext()
    } else expressions()

    private fun ifDeclaration(where: Token): Expression {
        expectType(Type.OPEN_CURVE)
        val logicalExpr = parseNext()
        expectType(Type.CLOSE_CURVE)
        val ifBody = autoBodyExpr()

        // All is Auto Scopped!
        if (isEOF() || peek().type != Type.ELSE)
            return Expression.If(where, logicalExpr, ifBody)
        skip()

        val elseBranch = when (peek().type) {
            Type.IF -> ifDeclaration(next())
            else -> autoBodyExpr()
        }
        return Expression.If(where, logicalExpr, ifBody, elseBranch)
    }

    private fun autoBodyExpr(): Expression.Scope {
        // used everywhere where there is no manual scope management is required,
        //  e.g., IfExpr, Until, For
        if (peek().type == Type.OPEN_CURLY) return autoScopeBody()
        resolver.enterScope()
        return Expression.Scope(parseNext(), resolver.leaveScope())
    }

    private fun autoScopeBody(): Expression.Scope {
        resolver.enterScope()
        return Expression.Scope(expressions(), resolver.leaveScope())
    }

    private fun unscoppedBodyExpr(): Expression {
        if (peek().type == Type.OPEN_CURLY) return expressions()
        return parseNext()
    }

    private fun expressions(): Expression {
        expectType(Type.OPEN_CURLY)
        val expressions = ArrayList<Expression>()
        if (peek().type == Type.CLOSE_CURLY)
            return Expression.ExpressionList(expressions)
        while (!isEOF() && peek().type != Type.CLOSE_CURLY)
            expressions.add(parseNext())
        expectType(Type.CLOSE_CURLY)
        if (expressions.size == 1) return expressions[0]
        return Expression.ExpressionList(expressions)
    }

    private fun variableDeclaration(where: Token): Expression {
        val name = readAlpha()
        val expr = if (!isNext(Type.COLON)) {
            Expression.AutoVariable(where, name, readVariableExpr())
        } else {
            skip()
            val definition = Expression.DefinitionType(name, readClassType())
            Expression.ExplicitVariable(
                where,
                where.type == Type.VAR,
                definition, readVariableExpr())
        }
        resolver.defineVariable(name, expr.type())
        return expr
    }

    private fun readClassType(): Type {
        val next = next()
        if (!next.hasFlag(Flag.CLASS))
            next.error<String>("Expected class type token")
        return next.type
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
            left = Expression.UnaryOperation(where, where.type, left, false)
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
                left = Expression.BinaryOperation(
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
            if (nextOp.type != Type.DOT
                && nextOp.type != Type.OPEN_CURVE &&
                nextOp.type != Type.OPEN_SQUARE
            ) break

            when (nextOp.type) {
                // calling shadow funcs
                Type.OPEN_CURVE -> left = unitCall(left)
                Type.OPEN_SQUARE -> {
                    // array access
                    skip()
                    val expr = parseNext()
                    expectType(Type.CLOSE_SQUARE)
                    left = Expression.ElementAccess(left.marking!!, left, expr)
                }

                else -> {
                    // calling a method in another class
                    // string.contains("melon")
                    skip()

                    val method = readAlpha()
                    val arguments = callArguments()
                    var static = false
                    if (left is Expression.Alpha)
                        static = resolver.classes.contains(left.value)
                    left = Expression.ClassMethodCall(left.marking!!, static, left, method, arguments)
                }
            }
        }
        return left
    }

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

            Type.OPEN_CURLY -> Expression.Shadow(emptyList(), autoScopeBody().expr)

            else -> {}
        }
        val token = next()
        if (token.hasFlag(Flag.VALUE)) {
            val alpha = parseValue(token)
            if (!isEOF() && peek().type == Type.OPEN_CURVE)
                return unitCall(alpha)
            return alpha
        } else if (token.hasFlag(Flag.UNARY)) {
            return Expression.UnaryOperation(token, token.type, parseTerm(), true)
        } else if (token.hasFlag(Flag.NATIVE_CALL)) {
            val arguments = callArguments()
            return Expression.NativeCall(token, token.type, arguments)
        }
        back()
        if (canParseNext()) return parseNext()
        return token.error("Unexpected token")
    }

    private fun parseValue(token: Token): Expression {
        return when (token.type) {
            Type.E_TRUE, Type.E_FALSE -> Expression.BoolLiteral(token, token.type == Type.E_TRUE)
            Type.E_INT -> Expression.IntLiteral(token, token.optionalData.toString().toInt())
            Type.E_STRING -> Expression.StringLiteral(token, token.optionalData as String)
            Type.E_CHAR -> Expression.CharLiteral(token, token.optionalData as Char)
            Type.ALPHA -> {
                val name = readAlpha(token)
                val vrReference = resolver.resolveVr(name)
                if (vrReference == null) {
                    // could be a function call or static invocation
                    if (resolver.resolveFn(name) != null || resolver.classes.contains(name))
                        Expression.Alpha(token, -2, name)
                    else token.error<Expression>("Could not resolve name $name")
                } else {
                    Expression.Alpha(token, vrReference.index, name, vrReference.exprType)
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
        val at = expectType(Type.OPEN_CURVE)
        val arguments = parseArguments()
        expectType(Type.CLOSE_CURVE)

        if (unitExpr is Expression.Alpha) {
            val name = unitExpr.value
            val fnExpr = resolver.resolveFn(name)
            if (fnExpr != null) {
                if (fnExpr.argsSize == -1)
                    throw RuntimeException("[Internal] Function args size is not yet set")
                if (fnExpr.argsSize != arguments.size)
                    at.error<String>("Fn [$name] expected ${fnExpr.argsSize} but got ${arguments.size}")
                // TODO: we have to test this later
                return Expression.MethodCall(unitExpr.marking!!, fnExpr, arguments)
            }
        }
        // TODO:
        //  we have to test them later,
        return Expression.ShadoInvoke(unitExpr.marking!!, unitExpr, arguments)
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
        return token.optionalData as String
    }

    private fun readAlpha(token: Token) =
        if (token.type == Type.ALPHA) token.optionalData as String
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