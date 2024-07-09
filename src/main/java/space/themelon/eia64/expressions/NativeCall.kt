package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type

data class NativeCall(
    val where: Token,
    val type: Type,
    val arguments: List<Expression>,
) : Expression(where) {

    override fun <R> accept(v: Visitor<R>) = v.nativeCall(this)

    override fun signature() = ExpressionSignature(
        when (type) {
            Type.PRINT, Type.PRINTLN -> ExpressionType.INT
            Type.READ, Type.READLN -> ExpressionType.STRING
            Type.SLEEP -> ExpressionType.INT
            Type.LEN -> ExpressionType.INT
            Type.FORMAT -> ExpressionType.STRING
            Type.INT_CAST -> ExpressionType.INT
            Type.STRING_CAST -> ExpressionType.STRING
            Type.BOOL_CAST -> ExpressionType.BOOL
            Type.TYPE -> ExpressionType.STRING
            Type.INCLUDE -> ExpressionType.BOOL
            // TODO: we need to figure out a better way
            Type.COPY -> arguments[0].signature().type
            Type.ARRAYOF, Type.ARRALLOC -> ExpressionType.ARRAY
            Type.TIME -> ExpressionType.INT
            Type.RAND -> ExpressionType.INT
            Type.EXIT -> ExpressionType.INT
            else -> throw RuntimeException("Unknown native call type $type")
        }
    )
}