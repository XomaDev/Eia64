package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature

data class FunctionExpr(
    val name: String,
    val arguments: List<Pair<String, Signature>>, // List< <Parameter Name, Sign> >
    val returnType: Signature,
    val body: Expression
) : Expression(null) {

    override fun <R> accept(v: Visitor<R>) = v.function(this)

    override fun sig() = Sign.NONE
}