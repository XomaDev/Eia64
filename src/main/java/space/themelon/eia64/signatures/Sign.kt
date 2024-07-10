package space.themelon.eia64.signatures

import space.themelon.eia64.syntax.Type

object Sign {

    private const val NONE_SIGN = "sig_none"
    private const val ANY_SIGN = "sig_any"
    private const val INT_SIGN = "sig_eint"
    private const val STRING_SIGN = "sig_string"
    private const val CHAR_SIGN = "sig_char"
    private const val BOOL_SIGN = "sig_bool"
    private const val ARRAY_SIGN = "sig_array"
    private const val UNIT_SIGN = "sig_unit"
    private const val OBJECT_SIGN = "sig_object"

    val NONE = SimpleSignature(NONE_SIGN)
    val ANY = SimpleSignature(ANY_SIGN)
    val INT = SimpleSignature(INT_SIGN)
    val STRING = SimpleSignature(STRING_SIGN)
    val CHAR = SimpleSignature(CHAR_SIGN)
    val BOOL = SimpleSignature(BOOL_SIGN)
    val ARRAY = SimpleSignature(ARRAY_SIGN)
    val UNIT = SimpleSignature(UNIT_SIGN)
    val OBJECT = SimpleSignature(OBJECT_SIGN)

    fun intoType(): Type {

    }
}