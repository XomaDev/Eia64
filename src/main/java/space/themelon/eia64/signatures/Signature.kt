package space.themelon.eia64.signatures

import space.themelon.eia64.signatures.SignatureConstants.JAVA
import space.themelon.eia64.syntax.Token

abstract class Signature {
    fun isInt() = this == SignatureConstants.INT
    fun isFloat() = this == SignatureConstants.FLOAT

    fun isNumeric() = this == SignatureConstants.NUM || this == SignatureConstants.INT || this == SignatureConstants.FLOAT
    fun isNumericOrChar() = isNumeric() || this == SignatureConstants.CHAR
    fun isJava() = this == JAVA || this is ClassSignature

    fun javaClass(where: Token) = javaClass() ?: where.error("Could not find Java package for sign '${logName()}'")

    fun javaClass(): Class<*> = Class.forName(when (this) {
        SignatureConstants.INT -> "java.lang.Integer"
        SignatureConstants.FLOAT -> "java.lang.Float"
        SignatureConstants.CHAR -> "java.lang.Character"
        SignatureConstants.STRING -> "java.lang.String"
        SignatureConstants.BOOL -> "java.lang.Boolean"
        SignatureConstants.LIST -> "java.util.ArrayList"
        SignatureConstants.DICT -> "java.util.HashMap"
        JAVA -> "java.lang.Object"
        is ClassSignature -> this.clazz.name
        else -> {
            println("Cannot convert to java class $this")
            null
        }
    })

    abstract fun logName(): String

    companion object {
        fun signFromJavaClass(clazz: Class<*>) = when (clazz.name) {
            "java.lang.Integer", "int" -> SignatureConstants.INT
            "java.lang.Boolean", "boolean" -> SignatureConstants.BOOL
            "java.lang.Float", "float" -> SignatureConstants.FLOAT
            "java.lang.Character", "char" -> SignatureConstants.CHAR
            "java.lang.CharSequence", "java.lang.String" -> SignatureConstants.STRING
            "void" -> SignatureConstants.NONE
            else -> {
                if (clazz == java.util.List::class.java
                    || java.util.List::class.java.isAssignableFrom(clazz)) SignatureConstants.LIST
                else if (clazz == java.util.Map::class.java
                    || java.util.Map::class.java.isAssignableFrom(clazz)) SignatureConstants.DICT
                else ClassSignature(clazz)
            }
        }
    }
}