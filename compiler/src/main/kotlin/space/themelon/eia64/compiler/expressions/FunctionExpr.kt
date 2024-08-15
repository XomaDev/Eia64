package space.themelon.eia64.compiler.expressions

import space.themelon.eia64.compiler.Expression
import space.themelon.eia64.compiler.signatures.Matching.matches
import space.themelon.eia64.compiler.signatures.Sign
import space.themelon.eia64.compiler.signatures.Signature
import space.themelon.eia64.compiler.syntax.Token

data class FunctionExpr(
    val where: Token,
    val name: String,
    val arguments: List<Pair<String, Signature>>, // List< <Parameter Name, Sign> >
    val isVoid: Boolean,
    val returnSignature: Signature,
    val body: Expression // sig checked
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.function(this)

    override fun sig(): Signature {
        body.sig()
        val receivedSignature = if (body is ExpressionList) body.returnSig() else body.sig()
        if (!isVoid && !matches(returnSignature, receivedSignature)) {
            where.error<String>("Promised return signature $returnSignature but got $receivedSignature")
        }
        return Sign.NONE
    }
}