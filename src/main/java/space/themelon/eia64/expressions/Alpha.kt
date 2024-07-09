package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.ExpressionSignature
import space.themelon.eia64.analysis.ExpressionType
import space.themelon.eia64.analysis.VariableMetadata
import space.themelon.eia64.syntax.Token

data class Alpha(
    val where: Token,
    val index: Int,
    val value: String,
    val vrType: VariableMetadata? = null
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.alpha(this)
    override fun signature(): ExpressionSignature {
        if (vrType == null) return ExpressionSignature(ExpressionType.ANY)
        println("alpha $value vr type = $vrType")
        return ExpressionSignature(vrType.runtimeType, vrType)
    }
}