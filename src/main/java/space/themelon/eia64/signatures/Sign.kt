package space.themelon.eia64.signatures

import space.themelon.eia64.syntax.Type

object Sign {

    const val NONE_SIGN = "sig_none" // used for internal return types of expressions
    const val ANY_SIGN = "sig_any"
    const val INT_SIGN = "sig_eint"
    const val STRING_SIGN = "sig_string"
    const val CHAR_SIGN = "sig_char"
    const val BOOL_SIGN = "sig_bool"
    const val ARRAY_SIGN = "sig_array"
    const val UNIT_SIGN = "sig_unit"
    const val OBJECT_SIGN = "sig_object"

    // Always return new instance, since we are testing
    // Metadata could be altered of the original one's
    val NONE
        get() = SimpleSignature(NONE_SIGN)
    val ANY
        get() = SimpleSignature(ANY_SIGN)
    val INT
        get() = SimpleSignature(INT_SIGN)
    val STRING
        get() = SimpleSignature(STRING_SIGN)
    val CHAR
        get() = SimpleSignature(CHAR_SIGN)
    val BOOL
        get() = SimpleSignature(BOOL_SIGN)
    val ARRAY
        get() = SimpleSignature(ARRAY_SIGN)
    val UNIT
        get() = SimpleSignature(UNIT_SIGN)
    val OBJECT
        get() = SimpleSignature(OBJECT_SIGN)

    fun Signature.intoType(): Type {
        return when (this) {
            NONE -> throw RuntimeException("No equivalent type to NONE Sign")
            ANY -> Type.E_ANY
            INT -> Type.E_INT
            STRING -> Type.E_STRING
            CHAR -> Type.E_CHAR
            BOOL -> Type.E_BOOL
            ARRAY -> Type.E_ARRAY
            UNIT -> Type.E_UNIT
            OBJECT -> Type.E_OBJECT

            is ArrayExtension -> Type.E_ARRAY
            is ObjectExtension -> Type.E_OBJECT
            else -> throw RuntimeException("Unknown signature $this provided for translation into Type")
        }
    }
}