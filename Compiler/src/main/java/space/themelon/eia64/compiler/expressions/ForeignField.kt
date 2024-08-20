package space.themelon.eia64.compiler.expressions

import space.themelon.eia64.compiler.Expression
import space.themelon.eia64.compiler.ModuleInfo
import space.themelon.eia64.compiler.analysis.UniqueVariable
import space.themelon.eia64.compiler.signatures.Signature
import space.themelon.eia64.compiler.syntax.Token

data class ForeignField(
    val where: Token,
    val static: Boolean,
    val objectExpression: Expression,
    val property: String,
    val uniqueVariable: UniqueVariable,
    val moduleInfo: ModuleInfo,
): Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.classPropertyAccess(this)

    override fun sig(): Signature {
        objectExpression.sig()
        return uniqueVariable.signature
    }
}