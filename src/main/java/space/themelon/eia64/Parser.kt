package space.themelon.eia64

import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type

class Parser(private val tokens: List<Token>) {

    private var index = 0
    private val size = tokens.size

    val expressions = ArrayList<Expression>()

    init {
        while (!isEOF()) {
            expressions.add(parseNext())
        }
        expressions.forEach { println(it) }
    }

    private fun parseNext(): Expression {
        val token = next()
        return when (token.flags[0]) {
            Type.V_KEYWORD -> {
                variableDeclaration(token)
            }

            else -> {
                back()
                parseExpr(0)
            }
        }
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
            // [ operator, plus|negate|slash|asterisk ]
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
        val token = next()
        if (token.type == Type.OPEN_CURVE) {
            val expr = parseNext()
            eat(Type.CLOSE_CURVE)
            return expr
        }
        if (token.hasFlag(Type.VALUE)) {
            return if (!isEOF() && peek().type == Type.OPEN_CURVE) funcInvoke(token)
            else parseValue(token)
        } else if (token.hasFlag(Type.UNARY)) {
            return Expression.UnaryOperation(Expression.Operator(token.type), parseElement())
        }
        throw RuntimeException("Unexpected token $token")
    }

    private fun parseValue(token: Token): Expression {
        // [ value, alpha|c_int|c_bool|c_string ]
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
        return Expression.MethodCall(name, arguments)
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

    private fun eat(type: Type) {
        val next = next()
        if (next.type != type) {
            throw RuntimeException("Expected token type: $type, got token $next")
        }
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