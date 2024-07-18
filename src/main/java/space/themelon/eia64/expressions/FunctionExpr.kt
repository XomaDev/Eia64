package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.signatures.Sign
import space.themelon.eia64.signatures.Signature

data class FunctionExpr(
    val name: String,
    val arguments: List<Pair<String, Signature>>, // List< <Parameter Name, Sign> >
    val returnType: Signature,
    val body: Expression
) : Expression(null) {

    init {
        // TODO: Verifying correct returns of functions according to their signature
        //   1st PLAN:
        //     note: to keep it simple, there are no inline functions
        //     Iterate through all of the expressions until you find return expression
        //     If we need to make sure that in all conditions, the function *returns* somehow

        // 2nd PLAN:
        //  We need to make it such a way that the return expression
        //  when called sig() returns a meta data property that says it will return *it*
        //  this metadata property should be also carried over to the upper nodes
        //
        //  fn hi(a: Bool): Int {
        //   if (a) { return 0 }
        //   else { return 2 }
        //
        //  println(hi)
        //
        // Fn(name='hi', body={..
        //    if(cond=a,
        //       then=<Scope, Metada={return=Int}>{ return Int(0) } else=<>{ return Int(2) })
        // }

        val bodySignature = body.sig()
        println("Body signature: $bodySignature")
    }

    override fun <R> accept(v: Visitor<R>) = v.function(this)

    override fun sig() = Sign.NONE
}