package space.themelon.eia64

import space.themelon.eia64.analysis.*
import space.themelon.eia64.expressions.*
import space.themelon.eia64.expressions.Array
import space.themelon.eia64.expressions.Function
import space.themelon.eia64.syntax.Token

abstract class Expression(
    val marking: Token? = null
) {

    interface Visitor<R> {
        fun genericLiteral(literal: GenericLiteral): R
        fun intLiteral(intLiteral: IntLiteral): R
        fun boolLiteral(boolLiteral: BoolLiteral): R
        fun stringLiteral(stringLiteral: StringLiteral): R
        fun charLiteral(charLiteral: CharLiteral): R
        fun alpha(alpha: Alpha): R
        fun array(array: Array): R
        fun include(include: Include): R
        fun new(new: NewObj): R
        fun throwExpr(throwExpr: ThrowExpr): R
        fun variable(variable: ExplicitVariable): R
        fun autoVariable(autoVariable: AutoVariable): R
        fun shado(shadow: Shadow): R
        fun unaryOperation(expr: UnaryOperation): R
        fun binaryOperation(expr: BinaryOperation): R
        fun expressions(list: ExpressionList): R
        fun nativeCall(call: NativeCall): R
        fun cast(cast: Cast): R
        fun scope(scope: space.themelon.eia64.expressions.Scope): R
        fun methodCall(call: MethodCall): R
        fun classMethodCall(call: ClassMethodCall): R
        fun unitInvoke(shadoInvoke: ShadoInvoke): R
        fun until(until: Until): R
        fun itr(itr: Itr): R
        fun whenExpr(whenExpr: When): R
        fun forEach(forEach: ForEach): R
        fun forLoop(forLoop: ForLoop): R
        fun interruption(interruption: Interruption): R
        fun ifFunction(ifExpr: If): R
        fun function(function: Function): R
        fun elementAccess(access: ElementAccess): R
    }

    abstract fun <R> accept(v: Visitor<R>): R
    abstract fun signature(): ExpressionSignature
}