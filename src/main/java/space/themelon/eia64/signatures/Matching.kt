package space.themelon.eia64.signatures

import space.themelon.eia64.Expression
import space.themelon.eia64.syntax.Token

object Matching {
    fun matches(expect: Signature, got: Signature): Boolean {
        // Void != Any
        if (expect == Sign.ANY) return got != Sign.NONE
        if (expect is SimpleSignature) return expect == got

        if (expect is ArrayExtension) {
            if (got !is ArrayExtension) return false
            return expect.elementSignature == Sign.ANY
                    || expect.elementSignature == got.elementSignature
        }

        if (expect is ObjectExtension) {
            if (got !is ObjectExtension) return false
            if (expect.extensionClass == Sign.ANY.type
                || expect.extensionClass == Sign.OBJECT.type) return true
            return expect.extensionClass == got.extensionClass
        }
        return false
    }

    fun verifyNonVoids(expressions: List<Expression>, where: Token, message: String) {
        for (expression in expressions) {
            val signature = expression.sig()
            if (signature == Sign.NONE) {
                where.error<String>(message)
                throw RuntimeException()
            }
        }
    }
}