package space.themelon.eia64.signatures

data class ClassSign(
    val clazz: Class<*>
) : Signature() {
    override fun logName() = "ClassSign($clazz)"
}