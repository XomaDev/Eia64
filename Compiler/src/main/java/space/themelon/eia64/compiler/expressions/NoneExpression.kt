package space.themelon.eia64.compiler.expressions

import space.themelon.eia64.compiler.Expression
import space.themelon.eia64.compiler.signatures.Sign

class NoneExpression: Expression() {

    companion object {
        val INSTANCE = NoneExpression()
    }

    override fun <R> accept(v: Visitor<R>) = v.noneExpression()
    override fun sig() = Sign.NONE
}