package space.themelon.eia64

import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type

class Parser(private val tokens: List<Token>) {

    private var index = 0
    private val size = tokens.size

    val expressions = ArrayList<Expression>()

    init {
        while (!isEOF()) {
            val result = parseNext()
            println("result $result")
            expressions.add(result)
        }
        expressions.forEach { println(it) }
    }

    private fun parseNext(): Expression {
        val token = next()
        return when (token.flags[0]) {
            Type.NATIVE_CALL -> sysCall(token)
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

    private fun sysCall(token: Token): Expression {
        eat(Type.OPEN_CURVE)
        val arguments = parseArguments()
        eat(Type.CLOSE_CURVE)
        return Expression.NativeCall(token.type, Expression.ExpressionList(arguments))
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
        val name = next().optionalData as String
        eat(Type.OPEN_CURVE)
        val requiredArgs = ArrayList<Expression>()
        while (!isEOF() && peek().type != Type.CLOSE_CURVE) {
            requiredArgs.add(Expression.Alpha(eat(Type.ALPHA).optionalData as String))
            if (peek().type != Type.COMMA)
                break
            skip()
        }
        eat(Type.CLOSE_CURVE)
        return Expression.Function(name, Expression.ExpressionList(requiredArgs), readBody())
    }

    private fun ifDeclaration(token: Token): Expression {
        eat(Type.OPEN_CURVE)
        val logicalExpr = parseNext()
        eat(Type.CLOSE_CURVE)
        val ifBody = parseExpr(0)

        println("body=$ifBody")
        if (isEOF() || peek().type != Type.ELSE)
            return Expression.IfFunction(logicalExpr, ifBody)
        skip()

        val elseBranch = when (peek().type) {
            Type.IF -> ifDeclaration(next())
            else -> parseExpr(0)
        }
        println("else=$elseBranch")
        return Expression.IfFunction(logicalExpr, ifBody, elseBranch)
    }

    private fun readBody(): Expression {
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
        val name = next().optionalData as String
        eat(Type.ASSIGNMENT)
        val expr = parseNext()
        return Expression.Variable(name, expr)
    }

    private fun parseExpr(minPrecedence: Int): Expression {
        var left = parseElement()
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

    private fun operatorPrecedence(type: Type): Int {
        return when (type) {
            Type.BITWISE -> 1
            Type.LOGICAL -> 2
            Type.EQUALITY -> 3
            Type.RELATIONAL -> 4
            Type.BINARY -> 5
            Type.BINARY_PRECEDE -> 6
            else -> -1
        }
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
            return Expression.UnaryOperation(Expression.Operator(token.type), parseElement())
        }
        throw RuntimeException("Unexpected token $token")
    }

    private fun parseValue(token: Token): Expression {
        return when (token.type) {
            Type.C_BOOL -> {
                Expression.EBool(token.hasFlag(Type.E_TRUE))
            }

            Type.C_INT -> {
                Expression.EInt((token.optionalData as String).toInt())
            }

            Type.C_STRING -> {
                Expression.EString(token.optionalData as String)
            }

            Type.ALPHA -> {
                return Expression.Alpha(token.optionalData as String)
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
        val name = token.optionalData as String
        eat(Type.OPEN_CURVE)
        val arguments = parseArguments()
        eat(Type.CLOSE_CURVE)
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