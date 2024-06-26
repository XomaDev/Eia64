package space.themelon.eia64.analysis

import space.themelon.eia64.Config
import space.themelon.eia64.Expression
import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type

class Parser {

    private val nameResolver = NameResolver()

    private lateinit var tokens: List<Token>
    private var index = 0
    private var size = 0

    fun parse(tokens: List<Token>): Expression.ExpressionList {
        index = 0
        size = tokens.size
        this.tokens = tokens

        val expressions = ArrayList<Expression>()
        while (!isEOF()) expressions.add(parseNext())
        if (Config.DEBUG) expressions.forEach { println(it) }
        return Expression.ExpressionList(expressions)
    }

    private fun parseNext(): Expression {
        val token = next()
        if (token.flags.isNotEmpty()) {
            if (token.flags[0] == Type.LOOP) return loop(token)
            else if (token.flags[0] == Type.V_KEYWORD) return variableDeclaration(token)
            else if (token.flags[0] == Type.INTERRUPTION) return interruption(token)
        }
        return when (token.type) {
            Type.IF -> ifDeclaration(token)
            Type.FUN -> fnDeclaration()
            Type.STDLIB -> importStdLib()
            else -> {
                back()
                return parseExpr(0)
            }
        }
    }

    private fun importStdLib(): Expression.ImportStdLib {
        expectType(Type.OPEN_CURVE)
        val imports = ArrayList<String>()
        while (true) {
            val className = readAlpha()
            imports.add(className)
            nameResolver.classes.add(className)
            if (peek().type == Type.CLOSE_CURVE) {
                skip()
                break
            }
            expectType(Type.COMMA)
        }
        return Expression.ImportStdLib(imports)
    }

    private fun loop(token: Token): Expression {
        when (token.type) {
            Type.UNTIL -> {
                expectType(Type.OPEN_CURVE)
                val expr = parseNext()
                expectType(Type.CLOSE_CURVE)
                val body = bodyOrExpr()
                return Expression.Until(expr, body)
            }
            Type.FOR -> {
                expectType(Type.OPEN_CURVE)
                val initializer = if (isNext(Type.COMMA)) null else parseNext()
                expectType(Type.COMMA)
                val conditional = if (isNext(Type.COMMA)) null else parseNext()
                expectType(Type.COMMA)
                val operational = if (isNext(Type.CLOSE_CURVE)) null else parseNext()
                expectType(Type.CLOSE_CURVE)
                val body = bodyOrExpr()
                return Expression.ForLoop(
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
                    return Expression.Itr(iName, from, to, by, bodyOrExpr())
                } else {
                    expectType(Type.IN)
                    val entity = parseNext()
                    expectType(Type.CLOSE_CURVE)
                    nameResolver.enterScope()
                    nameResolver.defineVr(iName)
                    val body = bodyOrExpr(false)
                    nameResolver.leaveScope()
                    return Expression.ForEach(iName, entity, body)
                }
            }
            else -> return token.error("Unknown loop type symbol")
        }
    }

    private fun interruption(token: Token) = Expression.Interruption(
        Expression.Operator(token.type),
        when (token.type) {
            Type.RETURN -> parseNext()
            else -> null
        }
    )

    private fun fnDeclaration(): Expression {
        val name = readAlpha()

        // create a wrapper object, that can be set to actual value later
        val fnElement = FnElement()
        nameResolver.defineFn(name, fnElement)
        nameResolver.enterScope()

        expectType(Type.OPEN_CURVE)
        val requiredArgs = ArrayList<Expression.DefinitionType>()
        while (!isEOF() && peek().type != Type.CLOSE_CURVE) {
            val parameterName = readAlpha()
            nameResolver.defineVr(parameterName)
            expectType(Type.COLON)
            val clazz = expectFlag(Type.CLASS).type

            requiredArgs.add(Expression.DefinitionType(parameterName, clazz))
            if (!isNext(Type.COMMA)) break
            skip()
        }
        fnElement.argsSize = requiredArgs.size
        expectType(Type.CLOSE_CURVE)
        val returnType = if (isNext(Type.COLON)) {
            skip()
            expectFlag(Type.CLASS).type
        } else Type.E_ANY
        val body = if (isNext(Type.ASSIGNMENT)) {
            skip()
            parseNext()
        } else optimiseExpr(body(false))
        nameResolver.leaveScope()
        val fnExpr = Expression.Function(name, requiredArgs, returnType, body)
        fnElement.fnExpression = fnExpr
        return fnExpr
    }

    private fun ifDeclaration(token: Token): Expression {
        expectType(Type.OPEN_CURVE)
        val logicalExpr = parseNext()
        expectType(Type.CLOSE_CURVE)
        val ifBody = bodyOrExpr()

        if (isEOF() || peek().type != Type.ELSE)
            return Expression.If(logicalExpr, ifBody)
        skip()

        val elseBranch = when (peek().type) {
            Type.IF -> ifDeclaration(next())
            else -> bodyOrExpr()
        }
        return Expression.If(logicalExpr, ifBody, elseBranch)
    }

    private fun bodyOrExpr(newScope: Boolean = true): Expression {
        if (peek().type == Type.OPEN_CURLY)
            return optimiseExpr(body(newScope))
        if (newScope) nameResolver.enterScope()
        val expr = parseNext()
        if (newScope) nameResolver.leaveScope()
        return expr
    }

    private fun body(createScope: Boolean = true): Expression.ExpressionList{
        if (createScope) nameResolver.enterScope()
        expectType(Type.OPEN_CURLY)
        val expressions = ArrayList<Expression>()
        while (!isEOF() && peek().type != Type.CLOSE_CURLY)
            expressions.add(parseNext())
        expectType(Type.CLOSE_CURLY)
        if (createScope) nameResolver.leaveScope()
        return Expression.ExpressionList(expressions)
    }

    private fun optimiseExpr(expr: Expression): Expression {
        if (expr !is Expression.ExpressionList) return expr
        if (expr.size != 1) return expr
        return expr.expressions[0]
    }

    private fun variableDeclaration(token: Token): Expression {
        val name = readAlpha()
        nameResolver.defineVr(name)
        if (!isNext(Type.COLON)) {
            expectType(Type.ASSIGNMENT)
            return Expression.AutoVariable(name, parseNext())
        }
        skip()
        val definition = Expression.DefinitionType(name, expectFlag(Type.CLASS).type)
        expectType(Type.ASSIGNMENT)
        return Expression.ExplicitVariable(token.type == Type.VAR, definition, parseNext())
    }

    private fun parseExpr(minPrecedence: Int): Expression {
        var left = parseElement()
        // a[x][y]
        // {{a, x}, y}
        while (!isEOF()) {
            val nextOp = peek()
            if (nextOp.type != Type.DOT && nextOp.type != Type.OPEN_SQUARE) break
            skip()
            if (nextOp.type == Type.OPEN_SQUARE) {
                val expr = parseNext()
                expectType(Type.CLOSE_SQUARE)
                left = Expression.ElementAccess(left, expr)
            } else {
                val method = readAlpha()
                expectType(Type.OPEN_CURVE)
                val arguments = parseArguments()
                expectType(Type.CLOSE_CURVE)
                var static = false
                if (left is Expression.Alpha)
                    static = nameResolver.classes.contains(left.value)
                left = Expression.ClassMethodCall(static, left, method, arguments)
            }
        }
        if (!isEOF() && peek().hasFlag(Type.POSSIBLE_RIGHT_UNARY))
            left = Expression.UnaryOperation(Expression.Operator(next().type), left, false)
        while (!isEOF()) {
            val opToken = peek()
            if (!opToken.hasFlag(Type.OPERATOR)) return left

            val precedence = operatorPrecedence(opToken.flags[0])
            if (precedence == -1) return left

            if (precedence >= minPrecedence) {
                skip() // operator token
                val right =
                    if (opToken.hasFlag(Type.NON_COMMUTE)) parseElement()
                    else parseExpr(precedence)
                left = Expression.BinaryOperation(
                    left,
                    right,
                    Expression.Operator(opToken.type)
                )
            } else return left
        }
        return left
    }

    private fun operatorPrecedence(type: Type) = when (type) {
        Type.ASSIGNMENT_TYPE -> 1
        Type.BITWISE -> 2
        Type.LOGICAL -> 3
        Type.EQUALITY -> 4
        Type.RELATIONAL -> 5
        Type.BINARY -> 6
        Type.BINARY_PRECEDE -> 7
        else -> -1
    }

    private fun parseElement(): Expression {
        when (peek().type) {
            Type.OPEN_CURVE -> {
                skip()
                val expr = parseNext()
                expectType(Type.CLOSE_CURVE)
                return expr
            }
            Type.OPEN_CURLY -> return body().also { it.preserveState = true }
            else -> { }
        }
        val token = next()
        if (token.hasFlag(Type.VALUE)) {
            return if (!isEOF() && peek().type == Type.OPEN_CURVE) funcInvoke(token)
            else parseValue(token)
        } else if (token.hasFlag(Type.UNARY)) {
            return Expression.UnaryOperation(Expression.Operator(token.type), parseElement(), true)
        } else if (token.hasFlag(Type.NATIVE_CALL)) {
            expectType(Type.OPEN_CURVE)
            val arguments = parseArguments()
            expectType(Type.CLOSE_CURVE)
            return Expression.NativeCall(token.type, Expression.ExpressionList(arguments))
        }
        back()
        return parseNext()
    }

    private fun parseValue(token: Token): Expression {
        return when (token.type) {
            Type.E_TRUE, Type.E_FALSE -> Expression.BoolLiteral(token.type == Type.E_TRUE)
            Type.E_INT -> Expression.IntLiteral(token.optionalData.toString().toInt())
            Type.E_STRING -> Expression.StringLiteral(token.optionalData as String)
            Type.E_CHAR -> Expression.CharLiteral(token.optionalData as Char)
            Type.ALPHA -> {
                val name = readAlpha(token)
                val vrIndex = nameResolver.resolveVr(name)
                if (vrIndex == -1) {
                    // could be a static invocation, search in dict
                    if (!nameResolver.classes.contains(name))
                        token.error<String>("Could not resolve name $name")
                    Expression.Alpha(-2, name)
                } else {
                    Expression.Alpha(vrIndex, name)
                }
            }
            Type.OPEN_CURVE -> {
                val expr = parseNext()
                expectType(Type.CLOSE_CURVE)
                expr
            }

            else -> token.error("Unknown token type: $token")
        }
    }

    private fun funcInvoke(token: Token): Expression {
        val name = readAlpha(token)
        expectType(Type.OPEN_CURVE)
        val arguments = parseArguments()
        expectType(Type.CLOSE_CURVE)
        val fnExpr = nameResolver.resolveFn(name)
        if (fnExpr.argsSize == -1)
            throw RuntimeException("[Internal] Function args size is not yet set")
        if (fnExpr.argsSize != arguments.size)
            token.error<String>("Fn [$name] expected ${fnExpr.argsSize} but got ${arguments.size}")
        return Expression.MethodCall(fnExpr, arguments)
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
        else token.error("Expected alpha token got $token")

    private fun expectType(type: Type): Token {
        val next = next()
        if (next.type != type)
            next.error<String>("Expected token type: $type, got token $next")
        return next
    }

    private fun expectFlag(flag: Type): Token {
        val next = next()
        if (!next.hasFlag(flag))
            next.error<String>("Expected flag: $flag, got $next")
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