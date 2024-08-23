package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.Matching.matches
import space.themelon.eia64.signatures.Matching.numericOrChar
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type

data class BinaryOperation(
    val where: Token,
    val left: Expression, // sig checked
    val right: Expression, // sig checked
    val operator: Type
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.binaryOperation(this)

    override fun sig(): Signature {
        val leftExprSign = left.sig()
        val rightExprSign = right.sig()

        val leftLogName = leftExprSign.logName()
        val rightLogName = rightExprSign.logName()

        var resultSign = leftExprSign
        if (operator == Type.PLUS) {
            if (!leftExprSign.isNumeric() && !rightExprSign.isNumeric()) resultSign = Sign.STRING
        } else if (operator == Type.NEGATE) {
            if (!leftExprSign.isNumeric() || !rightExprSign.isNumeric())
                applyError("arithmetic", "Numeric", "-")
        } else if (operator == Type.TIMES) {
            if (!leftExprSign.isNumeric() || !rightExprSign.isNumeric())
                applyError("arithmetic", "Numeric", "*")
        } else if (operator == Type.SLASH) {
            if (!leftExprSign.isNumeric() || !rightExprSign.isNumeric())
                applyError("arithmetic", "Numeric", "/")
        } else if (operator == Type.REMAINDER) {
            if (!leftExprSign.isNumeric() || !rightExprSign.isNumeric())
                applyError("arithmetic", "Remainder", "%")
        } else if (operator == Type.BITWISE_AND) {
            if (!leftExprSign.isNumeric() || !rightExprSign.isNumeric())
                applyError("bitwise", "Numeric", "&")
        } else if (operator == Type.BITWISE_OR) {
            if (!leftExprSign.isNumeric() || !rightExprSign.isNumeric())
                applyError("bitwise", "Numeric", "|")
        } else if (operator == Type.EQUALS || operator == Type.NOT_EQUALS) resultSign = Sign.BOOL
        else if (operator == Type.LOGICAL_AND) {
            if (leftExprSign != Sign.BOOL || rightExprSign != Sign.BOOL)
                applyError("logical", "Numeric", "&&")
        } else if (operator == Type.LOGICAL_OR) {
            if (leftExprSign != Sign.BOOL || rightExprSign != Sign.BOOL) {
                applyError("logical", "Numeric", "||")
            } else resultSign = Sign.BOOL
        } else if (operator == Type.RIGHT_DIAMOND) {
            if (!numericOrChar(left, right)) {
                where.error<String>("Cannot apply logical operator on non [Numeric/Char] expressions: ($leftLogName > $rightLogName)")
            } else resultSign = Sign.BOOL
        } else if (operator == Type.LEFT_DIAMOND) {
            if (!numericOrChar(left, right)) {
                where.error<String>("Cannot apply logical operator on non [Numeric/Char] expressions: ($leftLogName < $rightLogName)")
            } else resultSign = Sign.BOOL
        } else if (operator == Type.GREATER_THAN_EQUALS) {
            if (!numericOrChar(left, right)) {
                where.error<String>("Cannot apply logical operator on non [Numeric/Char] expressions: ($leftLogName >= $rightLogName)")
                resultSign = Sign.BOOL
            } else resultSign = Sign.BOOL
        } else if (operator == Type.LESSER_THAN_EQUALS) {
            if (!numericOrChar(left, right)) {
                where.error<String>("Cannot apply logical operator on [Numeric/Char] expressions: ($leftLogName <= $rightLogName)")
            } else resultSign = Sign.BOOL
        } else if (operator == Type.ASSIGNMENT) {
            if (left is ArrayAccess) {
                // Array Assignment Safety Check
                // Can we really assign *this type* to that *array type*?
                if (!matches(expect = leftExprSign, got = rightExprSign)) {
                    where.error<String>("Cannot assign type $rightExprSign to an array of type $leftExprSign")
                }
            }
            resultSign = rightExprSign
        }
        else if (operator == Type.ADDITIVE_ASSIGNMENT) if (rightExprSign == Sign.STRING || rightExprSign == Sign.CHAR) resultSign = Sign.STRING
        else if (rightExprSign == Sign.INT) resultSign = Sign.INT
        else if (rightExprSign == Sign.FLOAT) resultSign = Sign.FLOAT
        else where.error("Unknown expression signature for operator (+= Additive Assignment): $rightExprSign")
        else if (operator == Type.POWER) {
            if (!leftExprSign.isInt() || !rightExprSign.isInt()) applyError("arithmetic", "Numeric", "**")
        } else if (operator == Type.DEDUCTIVE_ASSIGNMENT) {
            if (!rightExprSign.isNumeric()) simpleApplyError("Numeric", "-=")
        } else if (operator == Type.MULTIPLICATIVE_ASSIGNMENT) {
            if (!rightExprSign.isNumeric()) simpleApplyError("Numeric", "*=")
        } else if (operator == Type.DIVIDIVE_ASSIGNMENT) {
            if (!rightExprSign.isNumeric()) simpleApplyError("Numeric", "/=")
        } else if (operator == Type.REMAINDER_ASSIGNMENT) {
            if (!rightExprSign.isNumeric()) simpleApplyError("Numeric", "%=")
        } else where.error("Unknown Binary Operator $operator")
        return resultSign
    }

    private fun applyError(group: String, type: String, operator: String) {
        where.error<String>("Cannot apply $group operator on non $type expressions: " +
                "(${left.sig().logName()} $operator ${right.sig().logName()})")
    }

    private fun simpleApplyError(type: String, operator: String) {
        where.error<String>("Expected $type expression for ($operator) but got ${right.sig().logName()}")
    }
}