package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.Matching.numericOrChar
import space.themelon.eia64.signatures.SignatureConstants
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type

data class BinaryOperation(
    val where: Token,
    val left: Expression, // sig checked
    val right: Expression, // sig checked
    val operator: Type
) : Expression() {

    override fun <R> accept(v: Visitor<R>) = v.binaryOperation(this)

    override fun sig(): Signature {
        val leftSig = left.sig()
        val rightSig = right.sig()

        val leftLogName = leftSig.logName()
        val rightLogName = rightSig.logName()

        var resultSign = leftSig
        when (operator) {
            Type.PLUS -> if (!leftSig.isNumeric() && !rightSig.isNumeric()) resultSign = SignatureConstants.STRING

            Type.NEGATE -> if (!leftSig.isNumeric() || !rightSig.isNumeric())
                applyError("arithmetic", "Numeric", "-", leftSig, rightSig)

            Type.TIMES -> if (!leftSig.isNumeric() || !rightSig.isNumeric())
                applyError("arithmetic", "Numeric", "*", leftSig, rightSig)

            Type.SLASH -> if (!leftSig.isNumeric() || !rightSig.isNumeric())
                applyError("arithmetic", "Numeric", "/", leftSig, rightSig)

            Type.REMAINDER -> if (!leftSig.isNumeric() || !rightSig.isNumeric())
                applyError("arithmetic", "Remainder", "%", leftSig, rightSig)

            Type.BITWISE_AND -> if (!leftSig.isNumeric() || !rightSig.isNumeric())
                applyError("bitwise", "Numeric", "&", leftSig, rightSig)

            Type.BITWISE_OR -> if (!leftSig.isNumeric() || !rightSig.isNumeric())
                applyError("bitwise", "Numeric", "|", leftSig, rightSig)

            Type.EQUALS, Type.NOT_EQUALS -> resultSign = SignatureConstants.BOOL

            Type.LOGICAL_AND -> if (leftSig != SignatureConstants.BOOL || rightSig != SignatureConstants.BOOL)
                applyError("logical", "Numeric", "&&", leftSig, rightSig)

            Type.LOGICAL_OR -> if (leftSig != SignatureConstants.BOOL || rightSig != SignatureConstants.BOOL) {
                applyError("logical", "Numeric", "||", leftSig, rightSig)
            } else resultSign = SignatureConstants.BOOL

            Type.RIGHT_DIAMOND -> if (!numericOrChar(left, right)) {
                where.error<String>("Cannot apply logical operator on non [Numeric/Char] expressions: ($leftLogName > $rightLogName)")
            } else resultSign = SignatureConstants.BOOL

            Type.LEFT_DIAMOND -> if (!numericOrChar(left, right)) {
                where.error<String>("Cannot apply logical operator on non [Numeric/Char] expressions: ($leftLogName < $rightLogName)")
            } else resultSign = SignatureConstants.BOOL

            Type.GREATER_THAN_EQUALS -> if (!numericOrChar(left, right)) {
                where.error<String>("Cannot apply logical operator on non [Numeric/Char] expressions: ($leftLogName >= $rightLogName)")
                resultSign = SignatureConstants.BOOL
            } else resultSign = SignatureConstants.BOOL

            Type.LESSER_THAN_EQUALS -> if (!numericOrChar(left, right)) {
                where.error<String>("Cannot apply logical operator on [Numeric/Char] expressions: ($leftLogName <= $rightLogName)")
            } else resultSign = SignatureConstants.BOOL

            Type.ASSIGNMENT -> resultSign = rightSig

            Type.ADDITIVE_ASSIGNMENT -> when (rightSig) {
                SignatureConstants.STRING, SignatureConstants.CHAR -> resultSign = SignatureConstants.STRING
                SignatureConstants.INT -> resultSign = SignatureConstants.INT
                SignatureConstants.FLOAT -> resultSign = SignatureConstants.FLOAT
                else -> where.error("Unknown expression signature for operator (+= Additive Assignment): $rightSig")
            }

            Type.POWER -> if (!leftSig.isInt() || !rightSig.isInt()) applyError("arithmetic", "Numeric", "**", leftSig, rightSig)

            Type.DEDUCTIVE_ASSIGNMENT -> if (!rightSig.isNumeric()) simpleApplyError("Numeric", "-=", rightSig)
            Type.MULTIPLICATIVE_ASSIGNMENT -> if (!rightSig.isNumeric()) simpleApplyError("Numeric", "*=", rightSig)
            Type.DIVIDIVE_ASSIGNMENT -> if (!rightSig.isNumeric()) simpleApplyError("Numeric", "/=", rightSig)
            Type.REMAINDER_ASSIGNMENT -> if (!rightSig.isNumeric()) simpleApplyError("Numeric", "%=", rightSig)

            else -> where.error("Unknown Binary Operator $operator")
        }
        return resultSign
    }

    private fun applyError(group: String, type: String, operator: String, left: Signature, right: Signature) {
        where.error<String>("Cannot apply $group operator on non $type expressions: " +
                "(${left.logName()} $operator ${right.logName()})")
    }

    private fun simpleApplyError(type: String, operator: String, right: Signature) {
        where.error<String>("Expected $type expression for ($operator) but got ${right.logName()}")
    }
}