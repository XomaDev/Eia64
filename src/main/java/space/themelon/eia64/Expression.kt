package space.themelon.eia64

import space.themelon.eia64.syntax.Type

abstract class Expression(private val representation: String) {

    interface Visitor<R> {
        fun eBool(bool: EBool): R
        fun eInt(eInt: EInt): R
        fun eString(eString: EString): R
        fun alpha(alpha: Alpha): R
        fun operator(operator: Operator): R
        fun unaryOperation(expr: UnaryOperation): R
        fun binaryOperation(expr: BinaryOperation): R
        fun variable(variable: Variable): R
        fun expressions(list: ExpressionList): R
        fun methodCall(call: MethodCall): R
        fun until(until: Until): R
        fun nativeReadWrite(call: NativeReadWrite): R
        fun interruption(interruption: Interruption): R
        fun ifFunction(ifExpr: If): R
        fun function(function: Function): R
    }

    abstract fun <R> accept(v: Visitor<R>): R

    override fun toString() = representation

    open class EBool(val value: Boolean) : Expression("Int($value)") {
        override fun <R> accept(v: Visitor<R>) = v.eBool(this)
    }

    open class EInt(val value: Int) : Expression("Int($value)") {
        override fun <R> accept(v: Visitor<R>) = v.eInt(this)
    }

    open class EString(val value: String) : Expression("String($value)") {
        override fun <R> accept(v: Visitor<R>) = v.eString(this)
    }

    open class Alpha(val value: String) : Expression("Alpha($value)") {
        override fun <R> accept(v: Visitor<R>) = v.alpha(this)
    }

    open class Operator(val value: Type) : Expression("OP($value)") {
        override fun <R> accept(v: Visitor<R>) = v.operator(this)
    }

    open class UnaryOperation(
        val operator: Operator,
        val expr: Expression,
        val left: Boolean
    ) : Expression("Unary($left, $operator, $expr)") {
        override fun <R> accept(v: Visitor<R>) = v.unaryOperation(this)
    }

    open class BinaryOperation(
        val left: Expression,
        val right: Expression,
        val operator: Operator) : Expression("BinaryOperation($operator, $left, $right)") {
        override fun <R> accept(v: Visitor<R>) = v.binaryOperation(this)
    }

    open class Variable(
        val name: String,
        val expr: Expression) : Expression("Variable($name, $expr)") {
        override fun <R> accept(v: Visitor<R>) = v.variable(this)
    }

    open class MethodCall(
        val name: String,
        val arguments: ExpressionList,
    ) : Expression("MethodCall($name, $arguments)") {
        override fun <R> accept(v: Visitor<R>) = v.methodCall(this)
    }

    open class NativeReadWrite(
        val type: Type,
        val arguments: ExpressionList,
    ) : Expression("NativeCall($type, $arguments)") {
        override fun <R> accept(v: Visitor<R>) = v.nativeReadWrite(this)
    }

    open class ExpressionList(
        val expressions: List<Expression>
    ) : Expression("List($expressions)") {
        val size = expressions.size
        override fun <R> accept(v: Visitor<R>) = v.expressions(this)
    }

    open class Until(
        val expression: Expression,
        val body: Expression
    ) : Expression("Until($expression, $body)") {
        override fun <R> accept(v: Visitor<R>) = v.until(this)
    }

    open class If(
        val condition: Expression,
        val thenBranch: Expression,
        val elseBranch: Expression? = null,
    ) : Expression("IfFunction($condition, $thenBranch, $elseBranch)") {
        override fun <R> accept(v: Visitor<R>) = v.ifFunction(this)
    }

    open class Interruption(val type: Operator, val expr: Expression? = null)
        : Expression("Interruption($type, $expr)") {
        override fun <R> accept(v: Visitor<R>) = v.interruption(this)
    }

    open class Function(
        val name: String,
        val arguments: List<String>,
        val body: Expression
    ) : Expression("Function($name, $arguments, $body)") {
        override fun <R> accept(v: Visitor<R>) = v.function(this)
    }

}