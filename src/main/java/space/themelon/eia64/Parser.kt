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
        return when (token.firstType) {
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
        val name = next().symbol!!
        eat(Type.EQUALS)
        val expr = parseNext()
        return Expression.Variable(name, expr)
    }

    private fun parseExpr(minPrecedence: Int): Expression {
        var left = parseElement()
        while (!isEOF()) {
            // [ operator, plus|negate|slash|asterisk ]
            if (peek().firstType != Type.OPERATOR)
                return left

            val opToken = next()
            val operator = opToken.types[1]
            val precedence = operatorPrecedence(operator)
            if (precedence == -1)
                return left

            if (precedence >= minPrecedence) {
                val right = if (opToken.hasType(Type.FLAG_NON_COMMUTE))
                        parseElement()
                    else parseExpr(precedence)
                left = Expression.BinaryOperation(
                    left,
                    right,
                    Expression.Operator(operator)
                )
            } else break
        }
        return left
    }

    private fun operatorPrecedence(type: Type): Int {
        return when (type) {
            Type.PLUS, Type.NEGATE -> 1
            Type.ASTERISK, Type.SLASH -> 2
            else -> -1
        }
    }

    private fun parseElement(): Expression {
        val token = next()
        return when (token.firstType) {
            Type.VALUE -> {
                if (!isEOF() && peek().hasType(Type.OPEN_CURVE))
                    funcInvoke(token)
                else parseValue(token)
            }

            Type.S_OPERATOR -> {
                parseSpecial(token)
            }

            else -> {
                throw RuntimeException("Unknown token type: $token")
            }
        }
    }

    private fun parseValue(token: Token): Expression {
        // [ value, alpha|c_int|c_bool|c_string ]
        return when (token.types[1]) {
            Type.C_BOOL -> {
                Expression.EBool(token.hasType(Type.E_TRUE))
            }

            Type.C_INT -> {
                Expression.EInt(token.symbol!!.toInt())
            }

            Type.C_STRING -> {
                Expression.EString(token.symbol!!)
            }

            Type.ALPHA -> {
                return Expression.Alpha(token.symbol!!)
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

    private fun parseSpecial(token: Token): Expression {
        // [ s_operator equals|open_curve|close_curve|comma ]
        when (token.types[1]) {
            Type.OPEN_CURVE -> {
                val expr = parseNext()
                eat(Type.CLOSE_CURVE)
                return expr
            }

            else -> {
                throw RuntimeException("Unexpected parse special token: $token")
            }
        }
    }

    private fun funcInvoke(token: Token): Expression {
        val name = token.symbol!!
        eat(Type.OPEN_CURVE)
        val arguments = parseArguments()
        eat(Type.CLOSE_CURVE)
        return Expression.MethodCall(name, arguments)
    }

    private fun parseArguments(): List<Expression> {
        val expressions = ArrayList<Expression>()
        if (!isEOF() && peek().hasType(Type.CLOSE_CURVE))
            return expressions
        while (!isEOF()) {
            expressions.add(parseNext())
            if (!peek().hasType(Type.COMMA))
                break
        }
        return expressions
    }

    private fun eat(type: Type) {
        val next = next()
        if (!next.hasType(type)) {
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