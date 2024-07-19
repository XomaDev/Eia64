package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.Matching.matches
import space.themelon.eia64.signatures.Consumable
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type

data class BinaryOperation(
    val where: Token,
    @Consumable("Cannot apply binary operation on void element") val left: Expression,
    @Consumable("Cannot apply binary operation on void element") val right: Expression,
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
            Type.PLUS -> if (leftExprSign != Sign.INT && rightExprSign != Sign.INT) resultSign = Sign.STRING

            Type.NEGATE -> if (leftExprSign != Sign.INT && rightExprSign != Sign.INT)
                where.error<String>("Cannot apply operator (- Minus) on non Int expressions")

            Type.TIMES -> if (leftExprSign != Sign.INT && rightExprSign != Sign.INT)
                where.error<String>("Cannot apply operator (* Times) on non Int expressions")

            Type.SLASH -> if (leftExprSign != Sign.INT && rightExprSign != Sign.INT)
                where.error<String>("Cannot apply operator (/ Divide) on non Int expressions")

            Type.BITWISE_AND -> if (leftExprSign != Sign.INT && rightExprSign != Sign.INT)
                where.error<String>("Cannot apply operator (& Bitwise And) on non Int expressions")

            Type.BITWISE_OR -> if (leftExprSign != Sign.INT && rightExprSign != Sign.INT)
                where.error<String>("Cannot apply operator (| Bitwise Or) on non Int expressions")

            Type.EQUALS, Type.NOT_EQUALS -> resultSign = Sign.BOOL

            Type.LOGICAL_AND -> if (leftExprSign != Sign.BOOL && rightExprSign != Sign.BOOL)
                where.error<String>("Cannot apply logical operator (&& Logical And) on non Bool expressions")

            Type.LOGICAL_OR -> if (leftExprSign != Sign.BOOL && rightExprSign != Sign.BOOL) {
                where.error<String>("Cannot apply logical operator (|| Logical Or) on non Bool expressions")
            } else resultSign = Sign.BOOL

            Type.RIGHT_DIAMOND -> if (leftExprSign != Sign.INT && rightExprSign != Sign.INT) {
                where.error<String>("Cannot apply logical operator (> Greater Than) on non Int expressions")
            } else resultSign = Sign.BOOL

            Type.LEFT_DIAMOND -> if (leftExprSign != Sign.INT && rightExprSign != Sign.INT) {
                where.error<String>("Cannot apply logical operator (< Lesser Than) on non Int expressions")
            } else resultSign = Sign.BOOL

            Type.GREATER_THAN_EQUALS -> if (leftExprSign != Sign.INT && rightExprSign != Sign.INT) {
                where.error<String>("Cannot apply logical operator (>= Greater Than Equals) on non Int expressions")
                resultSign = Sign.BOOL
            } else resultSign = Sign.BOOL

            Type.LESSER_THAN_EQUALS -> if (leftExprSign != Sign.INT && rightExprSign != Sign.INT) {
                where.error<String>("Cannot apply logical operator (<= Lesser Than Equals) on non Int expressions")
            } else resultSign = Sign.BOOL

            Type.ASSIGNMENT -> {
                if (left is ArrayAccess) {
                    // Array Assignment Safety Check
                    // Can we really assign *this type* to that *array type*?
                    if (!matches(expect = leftExprSign, got = rightExprSign)) {
                        where.error<String>("Cannot assign type $rightExprSign to an array of type $leftExprSign")
                    }
                }
                resultSign = rightExprSign
            }

            Type.ADDITIVE_ASSIGNMENT -> when (rightExprSign) {
                Sign.STRING, Sign.CHAR -> resultSign = Sign.STRING
                Sign.INT -> resultSign = Sign.INT
                else -> where.error("Unknown expression signature for operator (+= Additive Assignment): $rightExprSign")
            }

            Type.POWER -> if (leftExprSign != Sign.INT || rightExprSign != Sign.INT)
                where.error<String>("Value for operation (** Power) requires Int expression")

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