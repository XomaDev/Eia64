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
                eat(Type.OPEN_CURVE)
                val expr = parseNext()
                eat(Type.CLOSE_CURVE)
                val body = readBody()
                return Expression.Until(expr, body)
            }
            Type.FOR -> {
                eat(Type.OPEN_CURVE)
                val args = parseArguments(Type.COLON)
                if (args.size != 3) throw RuntimeException("[For-Loop] Expected 3 expressions (initializer:conditional:operational)")
                eat(Type.CLOSE_CURVE)
                val expr = Expression.ForLoop(
                    initializer = args[0],
                    conditional = args[1],
                    operational = args[2],
                    readBody()
                )
                return expr
            }
            Type.ITR -> {
                eat(Type.OPEN_CURVE)
                val iName = readAlpha()
                if (peek().type == Type.COLON) {
                    skip()
                    val from = parseNext()
                    eat(Type.TO)
                    val to = parseNext()

                    var by: Expression? = null
                    if (peek().type == Type.BY) {
                        skip()
                        by = parseNext()
                    }
                    println("to=$to")
                    eat(Type.CLOSE_CURVE)
                    return Expression.Itr(iName, from, to, by, readBody())
                } else {
                    eat(Type.IN)
                    val entity = parseNext()
                    eat(Type.CLOSE_CURVE)
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
        eat(Type.OPEN_CURVE)
        val requiredArgs = ArrayList<String>()
        while (!isEOF() && peek().type != Type.CLOSE_CURVE) {
            requiredArgs.add(readAlpha())
            if (peek().type != Type.COMMA)
                break
            skip()
        }
        eat(Type.CLOSE_CURVE)
        return Expression.Function(name, requiredArgs, readBody(allowAssign = true))
    }

    private fun ifDeclaration(token: Token): Expression {
        eat(Type.OPEN_CURVE)
        val logicalExpr = parseNext()
        eat(Type.CLOSE_CURVE)
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
        eat(Type.OPEN_CURLY)
        val expressions = ArrayList<Expression>()
        while (!isEOF() && peek().type != Type.CLOSE_CURLY)
            expressions.add(parseNext())
        eat(Type.CLOSE_CURLY)
        if (expressions.size == 1)
            return expressions[0]
        return Expression.ExpressionList(expressions)
    }

    private fun variableDeclaration(token: Token): Expression {
        // for now, we do not care about 'let' or 'var'
        val name = readAlpha()
        eat(Type.ASSIGNMENT)
        val expr = parseNext()
        return Expression.Variable(name, expr)
    }

    private fun parseExpr(minPrecedence: Int): Expression {
        var left = parseElement()
        // a[x][y]
        // {{a, x}, y}
        while (!isEOF() && peek().type == Type.OPEN_SQUARE) {
            skip()
            val expr = parseNext()
            eat(Type.CLOSE_SQUARE)
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
                eat(Type.CLOSE_CURVE)
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
            eat(Type.OPEN_CURVE)
            val arguments = parseArguments(Type.COMMA)
            eat(Type.CLOSE_CURVE)
            return Expression.NativeCall(token.type, Expression.ExpressionList(arguments))
        }
        throw RuntimeException("Unexpected token $token")
    }

    private fun parseValue(token: Token): Expression {
        return when (token.type) {
            Type.E_TRUE, Type.E_FALSE -> {
                Expression.EBool(token.type == Type.E_TRUE)
            }

            Type.C_INT -> {
                Expression.EInt(token.optionalData.toString().toInt())
            }

            Type.C_STRING -> {
                Expression.EString(token.optionalData.toString())
            }

            Type.ALPHA -> {
                return Expression.Alpha(readAlpha(token))
            }

            Type.OPEN_CURVE -> {
                val expr = parseNext()
                eat(Type.CLOSE_CURVE)
                return expr
            }

            else -> {
                throw RuntimeException("Unknown token type: $token")
            }
        }
    }

    private fun funcInvoke(token: Token): Expression {
        val name = readAlpha(token)
        eat(Type.OPEN_CURVE)
        val arguments = parseArguments(Type.COMMA)
        eat(Type.CLOSE_CURVE)
        return Expression.MethodCall(name, Expression.ExpressionList(arguments))
    }

    private fun parseArguments(separator: Type): List<Expression> {
        val expressions = ArrayList<Expression>()
        if (!isEOF() && peek().type == Type.CLOSE_CURVE)
            return expressions
        while (!isEOF()) {
            expressions.add(parseNext())
            if (peek().type != separator)
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

    private fun eat(type: Type): Token {
        val next = next()
        if (next.type != type) {
            throw RuntimeException("Expected token type: $type, got token $next")
        }
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