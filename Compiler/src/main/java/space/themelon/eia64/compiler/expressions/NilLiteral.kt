package space.themelon.eia64.compiler.expressions

import space.themelon.eia64.compiler.Expression
import space.themelon.eia64.compiler.signatures.Sign
import space.themelon.eia64.compiler.syntax.Token

data class NilLiteral(val where: Token): Expression(where) {
    override fun <R> accept(v: Visitor<R>) = v.nilLiteral(this)

    override fun sig() = Sign.NIL
}