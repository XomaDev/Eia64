package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.ScopeManager
import space.themelon.eia64.runtime.Environment
import space.themelon.eia64.signatures.ClassSign
import space.themelon.eia64.syntax.Token
import java.lang.reflect.Constructor

data class NewInstance(
    val clazz: Class<*>,
    val packageName: String,
    val constructor: Constructor<*>,
    val arguments: List<Expression>,
): Expression() {

    override fun <R> accept(v: Visitor<R>) = v.newJava(this)

    override fun sig() = ClassSign(clazz)
}