package space.themelon.eia64

import space.themelon.eia64.expressions.*
import space.themelon.eia64.expressions.FunctionExpr
import space.themelon.eia64.signatures.Signature

abstract class Expression {

    interface Visitor<R> {
        fun noneExpression(): R
        fun nilLiteral(nil: NilLiteral): R
        fun intLiteral(literal: IntLiteral): R
        fun floatLiteral(literal: FloatLiteral): R
        fun doubleLiteral(literal: DoubleLiteral): R
        fun boolLiteral(literal: BoolLiteral): R
        fun stringLiteral(literal: StringLiteral): R
        fun charLiteral(literal: CharLiteral): R
        fun makeList(makeList: MakeList): R
        fun makeDict(makeDict: MakeDictionary): R
        fun variable(variable: Variable): R
        fun alpha(alpha: Alpha): R
        fun array(literal: ArrayLiteral): R
        fun explicitArrayLiteral(arrayCreation: ExplicitArrayLiteral): R
        fun arrayAllocation(arrayAllocation: ArrayAllocation): R
        fun throwExpr(throwExpr: ThrowExpr): R
        fun isStatement(isStatement: IsStatement): R
        fun unaryOperation(expr: UnaryOperation): R
        fun binaryOperation(expr: BinaryOperation): R
        fun expressions(list: ExpressionList): R
        fun expressionBind(bind: ExpressionBind): R
        fun cast(cast: Cast): R
        fun scope(scope: Scope): R
        fun methodCall(call: MethodCall): R
        fun until(until: Until): R
        fun itr(itr: Itr): R
        fun whenExpr(whenExpr: When): R
        fun forEach(forEach: ForEach): R
        fun forLoop(forLoop: ForLoop): R
        fun interruption(interruption: Interruption): R
        fun ifFunction(ifExpr: IfStatement): R
        fun function(function: FunctionExpr): R
        fun arrayAccess(access: ArrayAccess): R

        fun newJava(newInstance: NewInstance): R
        fun javaFieldAccess(field: JavaField): R
        fun javaMethodCall(call: JavaMethodCall): R
    }

    abstract fun <R> accept(v: Visitor<R>): R
    abstract fun sig(): Signature
}