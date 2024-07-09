package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.ExpressionSignature
import space.themelon.eia64.analysis.ExpressionType
import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type

data class UnaryOperation(
    val where: Token,
    val operator: Type,
    val expr: Expression,
    val left: Boolean
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.unaryOperation(this)

        init {
            verify()
        }

        private fun verify() {
            val exprType = expr.signature()
            if (left) {
                when (operator) {
                    Type.NEGATE -> {
                        if (exprType.type == ExpressionType.INT) return
                        where.error<String>("Expected Int expression to apply (-) Negate Operator, got $exprType")
                    }

                    Type.INCREMENT -> {
                        if (exprType.type == ExpressionType.INT) return
                        where.error<String>("Expected Int expression to apply (++) Increment Operator, got $exprType")
                    }

                    Type.DECREMENT -> {
                        if (exprType.type == ExpressionType.INT) return
                        where.error<String>("Expected Int expression to apply (--) Decrement Operator, got $exprType")
                    }

                    Type.NOT -> {
                        if (exprType.type == ExpressionType.BOOL) return
                        where.error<String>("Expected Boolean expression to apply (!) Not Operator, got $exprType")
                    }

                    else -> {}
                }
            } else {
                when (operator) {
                    Type.INCREMENT -> {
                        if (exprType.type == ExpressionType.INT) return
                        where.error<String>("Expected Int expression to apply (++) Increment Operator, got $exprType")
                    }

                    Type.DECREMENT -> {
                        if (exprType.type == ExpressionType.INT) return
                        where.error<String>("Expected Int expression to apply (--) Decrement Operator, got $exprType")
                    }

                    else -> {}
                }
            }
            where.error<String>("Unknown operator")
        }

        override fun signature(): ExpressionSignature {
            val exprType = expr.signature()
            return ExpressionSignature(if (left) {
                when {
                    operator == Type.NEGATE && exprType.type == ExpressionType.INT -> ExpressionType.INT
                    operator == Type.INCREMENT && exprType.type == ExpressionType.INT -> ExpressionType.INT
                    operator == Type.DECREMENT && exprType.type == ExpressionType.INT -> ExpressionType.INT
                    operator == Type.NOT && exprType.type == ExpressionType.BOOL -> ExpressionType.BOOL
                    else -> ExpressionType.NONE
                }
            } else {
                when {
                    operator == Type.INCREMENT && exprType.type == ExpressionType.INT -> ExpressionType.INT
                    operator == Type.DECREMENT && exprType.type == ExpressionType.INT -> ExpressionType.INT
                    else -> ExpressionType.NONE
                }
            })
        }
    }