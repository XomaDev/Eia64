package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.Matching.matches
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Token

data class Variable(
  val where: Token,
  val name: String,
  val value: Expression,
  val promisedSignature: Signature? = null
) : Expression() {

  init {
    sig()
  }

  override fun <R> accept(v: Visitor<R>) = v.variable(this)

  override fun sig(): Signature {
    val exprSig = value.sig()
    if (promisedSignature == null) {
      return exprSig
    }
    if (!matches(expect = promisedSignature, got = exprSig)) {
      where.error<String>("Variable '$name' expected signature $promisedSignature but got $exprSig")
    }
    return promisedSignature
  }
}