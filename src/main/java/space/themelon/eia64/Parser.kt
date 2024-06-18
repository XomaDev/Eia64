package space.themelon.eia64

import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type

class Parser(private val tokens: List<Token>) {

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
                    Type.FUN -> fnDeclaration(token)
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
                val body = readBody()
                return Expression.Until(expr, body)
            }
            Type.FOR -> {
                expectType(Type.OPEN_CURVE)
                val initializer = if (peek().type == Type.COMMA) null else parseNext()
                expectType(Type.COMMA)
                val conditional = if (peek().type == Type.COMMA) null else parseNext()
                expectType(Type.COMMA)
                val operational = if (peek().type == Type.CLOSE_CURVE) null else parseNext()
                expectType(Type.CLOSE_CURVE)
                val expr = Expression.ForLoop(
                    initializer = initializer,
                    conditional = conditional,
                    operational = operational,
                    readBody()
                )
                return expr
            }
            Type.ITR -> {
                expectType(Type.OPEN_CURVE)
                val iName = readAlpha()
                if (peek().type == Type.COLON) {
                    skip()
                    val from = parseNext()
                    expectType(Type.TO)
                    val to = parseNext()

                    var by: Expression? = null
                    if (peek().type == Type.BY) {
                        skip()
                        by = parseNext()
                    }
                    expectType(Type.CLOSE_CURVE)
                    return Expression.Itr(iName, from, to, by, readBody())
                } else {
                    expectType(Type.IN)
                    val entity = parseNext()
                    expectType(Type.CLOSE_CURVE)
                    return Expression.ForEach(iName, entity, readBody())
                }
            }
            else -> throw RuntimeException("Unknown loop token ${token.type}")
        }
    }

    private fun interruption(token: Token): Expression.Interruption {
        return Expression.Interruption(
            Expression.Operator(token.type),
            when (token.type) {
                Type.RETURN -> parseNext()
                else -> null
            }
        )
    }

    private fun fnDeclaration(token: Token): Expression {
        val name = readAlpha()
        expectType(Type.OPEN_CURVE)
        val requiredArgs = ArrayList<Expression.DefinitionType>()
        while (!isEOF() && peek().type != Type.CLOSE_CURVE) {
            requiredArgs.add(readVarDefinition())
            if (peek().type != Type.COMMA)
                break
            skip()
        }
        expectType(Type.CLOSE_CURVE)
        return Expression.Function(name, requiredArgs, readBody(allowAssign = true))
    }

    private fun ifDeclaration(token: Token): Expression {
        expectType(Type.OPEN_CURVE)
        val logicalExpr = parseNext()
        expectType(Type.CLOSE_CURVE)
        val ifBody = parseNext()

        if (isEOF() || peek().type != Type.ELSE)
            return Expression.If(logicalExpr, ifBody)
        skip()

        val elseBranch = when (peek().type) {
            Type.IF -> ifDeclaration(next())
            else -> parseNext()
        }
        return Expression.If(logicalExpr, ifBody, elseBranch)
    }

    private fun readBody(allowAssign: Boolean = false): Expression {
        if (allowAssign && peek().type == Type.ASSIGNMENT) {
            skip()
            return Expression.Interruption(Expression.Operator(Type.RETURN), parseNext())
        }
        expectType(Type.OPEN_CURLY)
        val expressions = ArrayList<Expression>()
        while (!isEOF() && peek().type != Type.CLOSE_CURLY)
            expressions.add(parseNext())
        expectType(Type.CLOSE_CURLY)
        if (expressions.size == 1)
            return expressions[0]
        return Expression.ExpressionList(expressions)
    }

    private fun variableDeclaration(token: Token): Expression {
        // for now, we do not care about 'let' or 'var'
        val definition = readVarDefinition()
        expectType(Type.ASSIGNMENT)
        val expr = parseNext()
        return Expression.Variable(token.type == Type.VAR, definition, expr)
    }

    private fun readVarDefinition(): Expression.DefinitionType {
        // name:Type
        val name = readAlpha()
        expectType(Type.COLON)
        val clazz = expectFlag(Type.CLASS).type
        return Expression.DefinitionType(name, clazz)
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
            Type.OPEN_CURLY -> return readBody()
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
            Type.E_TRUE, Type.E_FALSE -> Expression.EBool(token.type == Type.E_TRUE)
            Type.C_INT -> Expression.EInt(token.optionalData.toString().toInt())
            Type.C_STRING -> Expression.EString(token.optionalData.toString())
            Type.ALPHA -> Expression.Alpha(readAlpha(token))

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
        return Expression.MethodCall(name, Expression.ExpressionList(arguments))
    }

    private fun parseArguments(): List<Expression> {
        val expressions = ArrayList<Expression>()
        if (!isEOF() && peek().type == Type.CLOSE_CURVE)
            return expressions
        while (!isEOF()) {
            expressions.add(parseNext())
            if (peek().type != Type.COMMA)
                break
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