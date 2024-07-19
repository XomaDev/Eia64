package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.SimpleSignature
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature
import space.themelon.eia64.syntax.Type

data class ExpressionList(
    val expressions: List<Expression>,
    var preserveState: Boolean = false,
) : Expression(null) {

    val size = expressions.size
    override fun <R> accept(v: Visitor<R>) = v.expressions(this)

    override fun sig(): Signature {
        for (expression in expressions) {
            if (expression is Interruption) {
                if (expression.operator == Type.RETURN) {
                    // a return statement, pass on it's signature
                    println("got return interruption: $expression")
                    return expression.sig()
                }
                break
            }
        }
        // return signature of last statement, this is what Kotlin does sometimes (I guess)
        // let a = if (true) {
        //   println("hello")
        //   7
        // } else {
        //   2
        // }
        // println(a)
        // ; 5
        println("yeah using last signature")
        return expressions.last().sig()
    }
}