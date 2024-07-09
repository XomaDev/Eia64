package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type

data class BinaryOperation(
    val where: Token,
    val left: Expression,
    val right: Expression,
    val operator: Type
    ) : Expression(where) {

        init {
            verify()
        }

        override fun <R> accept(v: Visitor<R>) = v.binaryOperation(this)

        private fun verify() {
            println("left = $left, right = $right, operator = $operator")
            val leftType = left.signature()
            val rightType = right.signature()

            when (operator) {
                Type.NEGATE,
                Type.TIMES,
                Type.SLASH,
                Type.BITWISE_AND,
                Type.BITWISE_OR,
                Type.RIGHT_DIAMOND,
                Type.LEFT_DIAMOND,
                Type.GREATER_THAN_EQUALS,
                Type.LESSER_THAN_EQUALS,
                Type.DEDUCTIVE_ASSIGNMENT,
                Type.MULTIPLICATIVE_ASSIGNMENT,
                Type.DIVIDIVE_ASSIGNMENT,
                Type.POWER -> {
                    if (leftType.type == ExpressionType.INT && rightType.type == ExpressionType.INT) return
                    where.error<String>("Cannot apply to a non Int expressions")
                }
                Type.LOGICAL_AND, Type.LOGICAL_OR -> {
                    println("left=${leftType.type}")
                    println("left=${rightType.type}")
                    if (leftType.type == ExpressionType.BOOL && rightType.type == ExpressionType.BOOL) return
                    where.error<String>("Cannot apply to a non Bool expressions")
                }

                Type.ADDITIVE_ASSIGNMENT -> {
                    if (leftType.type == ExpressionType.STRING
                        && (rightType.type == ExpressionType.STRING || rightType.type == ExpressionType.CHAR)
                        || leftType.type == ExpressionType.INT && rightType.type == ExpressionType.INT) return
                    where.error<String>("Cannot apply operator to a non String or Int")
                }
                Type.ASSIGNMENT -> left.signature()
                Type.EQUALS, Type.NOT_EQUALS, Type.PLUS -> { }
                else -> where.error("Undocumented binary operator")
            }
        }

        override fun signature(): ExpressionSignature {
            val leftType = left.signature().type
            val rightType = right.signature().type

            return ExpressionSignature(when (operator) {
                Type.PLUS -> {
                    if (leftType == ExpressionType.INT && rightType == ExpressionType.INT) ExpressionType.INT
                    else ExpressionType.STRING
                }

                Type.NEGATE, Type.TIMES, Type.SLASH, Type.BITWISE_AND, Type.BITWISE_OR -> ExpressionType.INT

                Type.EQUALS,
                Type.NOT_EQUALS,
                Type.LOGICAL_AND,
                Type.LOGICAL_OR,
                Type.RIGHT_DIAMOND,
                Type.LEFT_DIAMOND,
                Type.GREATER_THAN_EQUALS,
                Type.LESSER_THAN_EQUALS -> ExpressionType.BOOL

                Type.ASSIGNMENT -> left.signature().type // TODO: confirm this in future
                Type.ADDITIVE_ASSIGNMENT -> if (left.signature().type == ExpressionType.STRING) ExpressionType.STRING else ExpressionType.INT
                Type.DEDUCTIVE_ASSIGNMENT, Type.MULTIPLICATIVE_ASSIGNMENT, Type.DIVIDIVE_ASSIGNMENT, Type.POWER -> ExpressionType.INT

                else -> throw RuntimeException("Unknown operator $operator")
            })
        }
    }