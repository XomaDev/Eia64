package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.Signature

class DynamicLinkBody(
    val signature: Signature,
    val callback: (Array<Any>) -> Any
): Expression() {
    override fun <R> accept(v: Visitor<R>) = v.dynamicLinkBody(this)

    override fun sig() = signature
}