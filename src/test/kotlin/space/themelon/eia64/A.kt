import org.json.JSONObject
import kotlin.reflect.full.*
import java.net.HttpURLConnection
import java.net.URL
import kotlin.reflect.jvm.isAccessible

fun main() {
    println(unboxType(Class.forName("java.lang.Integer")))
}

private fun unboxType(boxedType: Class<*>): Class<*> = when (boxedType) {
    Boolean::class.java -> Boolean::class.javaPrimitiveType!!
    Byte::class.java -> Byte::class.javaPrimitiveType!!
    Char::class.java -> Char::class.javaPrimitiveType!!
    Short::class.java -> Short::class.javaPrimitiveType!!
    java.lang.Integer::class.java -> Int::class.javaPrimitiveType!!
    Long::class.java -> Long::class.javaPrimitiveType!!
    Float::class.java -> Float::class.javaPrimitiveType!!
    Double::class.java -> Double::class.javaPrimitiveType!!
    Void::class.java -> Void.TYPE
    else -> boxedType
}
