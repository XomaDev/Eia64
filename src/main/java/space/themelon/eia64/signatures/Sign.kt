package space.themelon.eia64.signatures

object Sign {
    val NONE = SimpleSignature("sig_none")
    val ANY = SimpleSignature("sig_any")
    val INT = SimpleSignature("sig_eint")
    val STRING = SimpleSignature("sig_string")
    val CHAR = SimpleSignature("sig_char")
    val BOOL = SimpleSignature("sig_bool")
    val ARRAY = SimpleSignature("sig_array")
    val UNIT = SimpleSignature("sig_unit")
    val OBJECT = SimpleSignature("sig_object")
}