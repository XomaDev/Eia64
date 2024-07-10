package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.SimpleSignature
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.syntax.Type

data class Function(
    val name: String,
    val arguments: List<Pair<Expression, String>>, // List< <Expression, Sign> >
    val returnType: Type,
    val body: Expression
) : Expression(null) {

    override fun <R> accept(v: Visitor<R>) = v.function(this)

    override fun sig() = Sign.NONE
}