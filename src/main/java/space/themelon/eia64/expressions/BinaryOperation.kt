package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.SimpleSignature
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type

data class BinaryOperation(
    val where: Token,
    val left: Expression,
    val right: Expression,
    val operator: Type
) : Expression(where) {

    init {
        sig()
    }

    override fun <R> accept(v: Visitor<R>) = v.binaryOperation(this)

    override fun sig(): Signature {
        val leftExprSign = left.sig()
        val rightExprSign = right.sig()

        var resultSign = leftExprSign
        when (operator) {
            Type.PLUS -> if (!(leftExprSign == Sign.INT && rightExprSign == Sign.INT)) resultSign = Sign.STRING

            Type.NEGATE -> if (!(leftExprSign == Sign.INT && rightExprSign == Sign.INT))
                where.error<String>("Cannot apply operator (- Minus) on non Int expressions")
            Type.TIMES -> if (!(leftExprSign == Sign.INT && rightExprSign == Sign.INT))
                where.error<String>("Cannot apply operator (* Times) on non Int expressions")
            Type.SLASH -> if (!(leftExprSign == Sign.INT && rightExprSign == Sign.INT))
                where.error<String>("Cannot apply operator (/ Divide) on non Int expressions")
            Type.BITWISE_AND -> if (!(leftExprSign == Sign.INT && rightExprSign == Sign.INT))
                where.error<String>("Cannot apply operator (& Bitwise And) on non Int expressions")
            Type.BITWISE_OR -> if (!(leftExprSign == Sign.INT && rightExprSign == Sign.INT))
                where.error<String>("Cannot apply operator (| Bitwise Or) on non Int expressions")

            Type.EQUALS, Type.NOT_EQUALS -> resultSign = Sign.BOOL

            Type.LOGICAL_AND -> if (!(leftExprSign == Sign.BOOL && rightExprSign == Sign.BOOL))
                where.error<String>("Cannot apply logical operator (&& Logical And) on non Bool expressions")
            Type.LOGICAL_OR -> if (!(leftExprSign == Sign.BOOL && rightExprSign == Sign.BOOL))
                where.error<String>("Cannot apply logical operator (|| Logical Or) on non Bool expressions")
            Type.RIGHT_DIAMOND -> if (!(leftExprSign == Sign.BOOL && rightExprSign == Sign.BOOL))
                where.error<String>("Cannot apply logical operator (> Greater Than) on non Bool expressions")
            Type.LEFT_DIAMOND -> if (!(leftExprSign == Sign.BOOL && rightExprSign == Sign.BOOL))
                where.error<String>("Cannot apply logical operator (< Lesser Than) on non Bool expressions")
            Type.GREATER_THAN_EQUALS -> if (!(leftExprSign == Sign.BOOL && rightExprSign == Sign.BOOL))
                where.error<String>("Cannot apply logical operator (>= Greater Than Equals) on non Bool expressions")
            Type.LESSER_THAN_EQUALS -> if (!(leftExprSign == Sign.BOOL && rightExprSign == Sign.BOOL))
                where.error<String>("Cannot apply logical operator (<= Lesser Than Equals) on non Bool expressions")

            Type.ASSIGNMENT -> resultSign = rightExprSign

            Type.ADDITIVE_ASSIGNMENT -> when (rightExprSign) {
                Sign.STRING -> resultSign = Sign.STRING
                Sign.INT -> resultSign = Sign.INT
                else -> where.error("Unknown expression signature for operator (+= Additive Assignment): $rightExprSign")
            }

            Type.DEDUCTIVE_ASSIGNMENT -> if (rightExprSign != Sign.INT)
                where.error<String>("Value for operation (-= Deductive Assignment) requires Int expression")
            Type.MULTIPLICATIVE_ASSIGNMENT -> if (rightExprSign != Sign.INT)
                where.error<String>("Value for operation (*= Times Assignment) requires Int expression")
            Type.DIVIDIVE_ASSIGNMENT -> if (rightExprSign != Sign.INT)
                where.error<String>("Value for operation (/= Dividive Assignment) requires Int expression")

            else -> where.error("Unknown Binary Operator $operator")
        }
        return resultSign
    }
}