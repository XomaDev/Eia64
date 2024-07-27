package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token

class QuantumExpression(
    marking: Token
): Expression() {

    // A fancy name! Hehe!
    // It's an expression, but it's not decided what it's gonna be
    // until just before the end of a particular scope
    override fun <R> accept(v: Visitor<R>): R {
        TODO("Not yet implemented")
    }

    override fun sig(): Signature {
        TODO("Not yet implemented")
    }
}