package space.themelon.eia64.signatures

import space.themelon.eia64.syntax.Type

object Sign {

    private const val NONE_SIGN = "sig_none" // used for internal return types of expressions
    private const val NIL_SIGN = "sig_nil" // for language
    private const val ANY_SIGN = "sig_any"
    private const val INT_SIGN = "sig_eint"
    private const val STRING_SIGN = "sig_string"
    private const val CHAR_SIGN = "sig_char"
    private const val BOOL_SIGN = "sig_bool"
    private const val ARRAY_SIGN = "sig_array"
    private const val UNIT_SIGN = "sig_unit"
    const val OBJECT_SIGN = "sig_object"

    // Always return new instance, since we are testing
    // Metadata could be altered of the original one's
    val NONE = SimpleSignature(NONE_SIGN)
    val NIL = SimpleSignature(NIL_SIGN)
    val ANY = SimpleSignature(ANY_SIGN)
    val INT = SimpleSignature(INT_SIGN)
    val STRING = SimpleSignature(STRING_SIGN)
    val CHAR = SimpleSignature(CHAR_SIGN)
    val BOOL = SimpleSignature(BOOL_SIGN)
    val ARRAY = SimpleSignature(ARRAY_SIGN)
    val UNIT = SimpleSignature(UNIT_SIGN)
    val OBJECT = SimpleSignature(OBJECT_SIGN)

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