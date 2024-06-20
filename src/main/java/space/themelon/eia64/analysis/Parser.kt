package space.themelon.eia64.analysis

import space.themelon.eia64.Config
import space.themelon.eia64.Expression
import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type

class Parser(private val tokens: List<Token>) {

    private val nameResolver = NameResolver()

    private var index = 0
    private val size = tokens.size

    private val expressions = ArrayList<Expression>()
    val parsedResult: Expression

    init {
        while (!isEOF()) {
            expressions.add(parseNext())
        }
        if (Config.DEBUG) expressions.forEach { println(it) }
        parsedResult = Expression.ExpressionList(expressions)
    }

    private fun parseNext(): Expression {
        val token = next()
        return when (token.flags[0]) {
            Type.LOOP -> loop(token)
            Type.V_KEYWORD -> variableDeclaration(token)
            Type.INTERRUPTION -> interruption(token)
            else -> {
                when (token.type) {
                    Type.IF -> ifDeclaration(token)
                    Type.FUN -> fnDeclaration()
                    else -> {
                        back()
                        parseExpr(0)
                    }
                }
            }
        }
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
                nameResolver.enterScope()
                val initializer = if (isNext(Type.COMMA)) null else parseNext()
                expectType(Type.COMMA)
                val conditional = if (isNext(Type.COMMA)) null else parseNext()
                expectType(Type.COMMA)
                val operational = if (isNext(Type.CLOSE_CURVE)) null else parseNext()
                expectType(Type.CLOSE_CURVE)
                val body = bodyOrExpr()
                nameResolver.leaveScope()
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
                    return Expression.ForEach(iName, entity, bodyOrExpr())
                }
            }
            else -> throw RuntimeException("Unknown loop token ${token.type}")
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
        nameResolver.defineFn(name)
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
        expectType(Type.CLOSE_CURVE)
        val returnType = if (isNext(Type.COLON)) {
            skip()
            expectFlag(Type.CLASS).type
        } else Type.C_ANY
        val body = if (isNext(Type.ASSIGNMENT)) {
            skip()
            parseNext()
        } else body(false)
        nameResolver.leaveScope()
        return Expression.Function(name, requiredArgs, returnType, body)
    }

    private fun ifDeclaration(token: Token): Expression {
        expectType(Type.OPEN_CURVE)
        val logicalExpr = parseNext()
        expectType(Type.CLOSE_CURVE)
        // TODO:
        //  need to handle bodies over here
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

    private fun bodyOrExpr(): Expression {
        if (peek().type == Type.OPEN_CURLY)
            return body(true)
        return parseNext()
    }

    private fun body(createScope: Boolean = true): Expression {
        if (createScope) nameResolver.enterScope()
        expectType(Type.OPEN_CURLY)
        val expressions = ArrayList<Expression>()
        while (!isEOF() && peek().type != Type.CLOSE_CURLY)
            expressions.add(parseNext())
        expectType(Type.CLOSE_CURLY)
        if (createScope) nameResolver.leaveScope()
        if (expressions.size == 1)
            return expressions[0]
        return Expression.ExpressionList(expressions)
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
        while (!isEOF() && peek().type == Type.OPEN_SQUARE) {
            skip()
            val expr = parseNext()
            expectType(Type.CLOSE_SQUARE)
            left = Expression.ElementAccess(left, expr)
        }
        if (!isEOF() && peek().hasFlag(Type.POSSIBLE_RIGHT_UNARY))
            left = Expression.UnaryOperation(Expression.Operator(next().type), left, false)
        while (!isEOF()) {
            if (!peek().hasFlag(Type.OPERATOR))
                return left

            val opToken = next()
            val precedence = operatorPrecedence(opToken.flags[0])
            if (precedence == -1)
                return left

            if (precedence >= minPrecedence) {
                val right = if (opToken.hasFlag(Type.NON_COMMUTE))
                    parseElement()
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
            Type.OPEN_CURLY -> return body()
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
        throw RuntimeException("Unexpected token $token")
    }

    private fun parseValue(token: Token): Expression {
        return when (token.type) {
            Type.E_TRUE, Type.E_FALSE -> Expression.Literal(token.type == Type.E_TRUE)
            Type.C_INT -> Expression.Literal(token.optionalData.toString().toInt())
            Type.C_STRING, Type.C_CHAR -> Expression.Literal(token.optionalData!!)
            Type.ALPHA -> {
                val name = readAlpha(token)
                Expression.Alpha(nameResolver.resolveVr(name), name)
            }

            Type.OPEN_CURVE -> {
                val expr = parseNext()
                expectType(Type.CLOSE_CURVE)
                expr
            }

            else -> throw RuntimeException("Unknown token type: $token")
        }
    }

    private fun funcInvoke(token: Token): Expression {
        val name = readAlpha(token)
        expectType(Type.OPEN_CURVE)
        val arguments = parseArguments()
        expectType(Type.CLOSE_CURVE)
        return Expression.MethodCall(nameResolver.resolveFn(name), name, Expression.ExpressionList(arguments))
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
        if (token.type != Type.ALPHA) throw RuntimeException("Expected alpha token got $token")
        return token.optionalData as String
    }

    private fun readAlpha(token: Token) =
            if (token.type == Type.ALPHA) token.optionalData as String
                    else throw RuntimeException("Expected alpha token got $token")

    private fun expectType(type: Type): Token {
        val next = next()
        if (next.type != type)
            throw RuntimeException("Expected token type: $type, got token $next")
        return next
    }

    private fun expectFlag(flag: Type): Token {
        val next = next()
        if (!next.hasFlag(flag))
            throw RuntimeException("Expected flag: $flag, got $next")
        return next
    }

    private fun isNext(type: Type) = peek().type == type

    private fun back() {
        index--
    }

    private fun skip() {
        index++
    }

    private fun next() = tokens[index++]
    private fun peek() = tokens[index]
    private fun isEOF() = index == size
}