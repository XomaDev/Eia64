package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.Signature

class PseudoFunction: Expression() {

    override fun <R> accept(v: Visitor<R>): R {
        throw NotImplementedError()
    }

    override fun sig(): Signature {
        throw NotImplementedError()
    }
}