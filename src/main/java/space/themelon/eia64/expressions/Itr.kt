package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.analysis.ExpressionSignature
import space.themelon.eia64.analysis.ExpressionType
import space.themelon.eia64.syntax.Token

data class Itr(
    val where: Token,
    val name: String,
    val from: Expression,
    val to: Expression,
    val by: Expression?,
    val body: Expression,
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.itr(this)
    override fun signature() = ExpressionSignature(ExpressionType.NONE)
}

data class When(
    val where: Token,
    val expr: Expression,
    val matches: List<Pair<Expression, Expression>>,
    val defaultBranch: Expression,
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.whenExpr(this)

    // TODO:
    //  in future we have to analyze all the types, if all of the types are equal, return them
    //  or else we return type ANY
    override fun signature() = ExpressionSignature(ExpressionType.ANY)
}