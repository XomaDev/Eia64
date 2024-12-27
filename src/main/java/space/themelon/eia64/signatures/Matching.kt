package space.themelon.eia64.signatures

import space.themelon.eia64.Expression
import space.themelon.eia64.syntax.Token

object Matching {

    fun numericOrChar(first: Expression, second: Expression) =
        first.sig().isNumericOrChar() && second.sig().isNumericOrChar()

    fun matches(expect: Signature, got: Signature): Boolean {
        if (expect == SignatureConstants.NUM) return got.isNumeric()
        if (expect == SignatureConstants.ARRAY && got is ArrayExtension) return true
        if (got == SignatureConstants.NIL) return true
        if (expect == SignatureConstants.ANY) return got != SignatureConstants.NONE
        if (expect is SimpleSignature)return expect == got

        if (expect is ArrayExtension) {
            if (got !is ArrayExtension) return false
            return expect.elementSignature == SignatureConstants.ANY
                    || expect.elementSignature == got.elementSignature
        }

        if (expect is ObjectExtension) {
            if (got !is ObjectExtension) return false
            if (expect.extensionClass == SignatureConstants.ANY.type) return true
            return expect.extensionClass == got.extensionClass
        }
        if (expect is ClassSignature) {
            if (got !is ClassSignature) return false
            return expect.clazz == got.clazz
        }
        return false
    }

    fun verifyNonVoids(expressions: List<Expression>, where: Token, message: String) {
        for (expression in expressions) {
            val signature = expression.sig()
            if (signature == SignatureConstants.NONE) {
                where.error<String>(message)
                throw RuntimeException()
            }
        }
    }
}