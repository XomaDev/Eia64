package space.themelon.eia64.expressions

import space.themelon.eia64.Expression

class QuantumExpression: Expression() {

    // A fancy name! Hehe!
    // It's an expression, but it's not decided what it's going to be
    // until just before the end of a particular scope.
    //
    //  Useful when we are referencing a function that appears
    //  later in the scope but pheasant been parsed yet

    // TODO:
    //  We would need to do de-cluttering, this is just an awaiting wrapper
    //  That is of no use at the runtime and adds redundancy
    //  Though this idea should be implemented after refactor

    var expression: Expression? = null

    override fun <R> accept(v: Visitor<R>) = expression!!.accept(v)

    override fun sig() = expression!!.sig()
}