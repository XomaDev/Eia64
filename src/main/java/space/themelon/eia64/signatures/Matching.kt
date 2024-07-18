package space.themelon.eia64.signatures

object Matching {
    fun matches(expect: Signature, got: Signature): Boolean {
        if (expect == Sign.ANY) return true
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
}