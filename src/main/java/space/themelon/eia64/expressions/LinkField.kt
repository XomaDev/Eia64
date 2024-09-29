package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.runtime.Entity
import space.themelon.eia64.signatures.Signature

data class LinkField(
    val signature: Signature,
    val entity: Entity
): Expression() {
    override fun <R> accept(v: Visitor<R>) = v.linkField(this)

    override fun sig() = signature
}