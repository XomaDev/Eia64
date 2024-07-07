package space.themelon.eia64.analysis

import space.themelon.eia64.Expression
import space.themelon.eia64.syntax.Type

data class FunctionReference(
    var fnExpression: Expression.Function? = null,
    var argsSize: Int = -1,
    val returnType: Type
)
