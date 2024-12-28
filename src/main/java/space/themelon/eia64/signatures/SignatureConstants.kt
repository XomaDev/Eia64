package space.themelon.eia64.signatures


object SignatureConstants {

    private const val NONE_SIGN = "sig_none" // used for internal return types of expressions
    private const val NIL_SIGN = "sig_nil" // for language
    private const val ANY_SIGN = "sig_any"
    private const val ARRAY_SIGN = "sig_array"
    private const val LIST_SIGN = "sig_list"
    private const val DICT_SIGN = "sig_dict"
    private const val UNIT_SIGN = "sig_unit"
    private const val JAVA_OBJECT = "sig_java"

    // Always return new instance, since we are testing
    // Metadata could be altered of the original one's
    val NONE = SimpleSignature(NONE_SIGN)
    val NIL = SimpleSignature(NIL_SIGN)
    val ANY = SimpleSignature(ANY_SIGN)

    val ARRAY = SimpleSignature(ARRAY_SIGN)
    val LIST = SimpleSignature(LIST_SIGN)
    val DICT = SimpleSignature(DICT_SIGN)
    val UNIT = SimpleSignature(UNIT_SIGN)
    val JAVA = SimpleSignature(JAVA_OBJECT)

    val NUM = ClassSignature(String::class.java)
    val INT = ClassSignature(Int::class.java)
    val FLOAT = ClassSignature(Float::class.java)
    val LONG = ClassSignature(Long::class.java)
    val DOUBLE = ClassSignature(Double::class.java)
    val STRING = ClassSignature(String::class.java)
    val CHAR = ClassSignature(Char::class.java)
    val BOOL = ClassSignature(Boolean::class.java)

}