package space.themelon.eia64.signatures

data class ClassSignature(
    val clazz: Class<*>
) : Signature() {
    override fun logName() = "ClassSign($clazz)"
}