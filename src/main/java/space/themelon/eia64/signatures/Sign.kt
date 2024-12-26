package space.themelon.eia64.signatures


object Sign {

    private const val NONE_SIGN = "sig_none" // used for internal return types of expressions
    private const val NIL_SIGN = "sig_nil" // for language
    private const val ANY_SIGN = "sig_any"
    private const val NUM_SIGN = "sig_num"
    private const val INT_SIGN = "sig_eint"
    private const val FLOAT_SIGN = "sig_float"
    private const val DOUBLE_SIGN = "sig_double"
    private const val STRING_SIGN = "sig_string"
    private const val CHAR_SIGN = "sig_char"
    private const val BOOL_SIGN = "sig_bool"
    private const val ARRAY_SIGN = "sig_array"
    private const val LIST_SIGN = "sig_list"
    private const val DICT_SIGN = "sig_dict"
    private const val UNIT_SIGN = "sig_unit"
    private const val CLASS_SIGN = "sig_class"
    private const val JAVA_OBJECT = "sig_java"

    // Always return new instance, since we are testing
    // Metadata could be altered of the original one's
    val NONE = SimpleSignature(NONE_SIGN)
    val NIL = SimpleSignature(NIL_SIGN)
    val ANY = SimpleSignature(ANY_SIGN)
    val NUM = SimpleSignature(NUM_SIGN)
    val INT = SimpleSignature(INT_SIGN)
    val FLOAT = SimpleSignature(FLOAT_SIGN)
    val DOUBLE = SimpleSignature(DOUBLE_SIGN)
    val STRING = SimpleSignature(STRING_SIGN)
    val CHAR = SimpleSignature(CHAR_SIGN)
    val BOOL = SimpleSignature(BOOL_SIGN)
    val ARRAY = SimpleSignature(ARRAY_SIGN)
    val LIST = SimpleSignature(LIST_SIGN)
    val DICT = SimpleSignature(DICT_SIGN)
    val UNIT = SimpleSignature(UNIT_SIGN)
    val TYPE = SimpleSignature(CLASS_SIGN)
    val JAVA = SimpleSignature(JAVA_OBJECT)

}