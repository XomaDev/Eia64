package space.themelon.eia64

import space.themelon.eia64.analysis.ExprType
import space.themelon.eia64.analysis.FnElement
import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type

abstract class Expression(
    private val where: Token? = null
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
        fun scope(scope: Scope): R
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
    abstract fun type(): ExprType

    // for internal evaluation use
    data class GenericLiteral(
        val where: Token,
        val value: Any): Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.genericLiteral(this)
        override fun type() = ExprType.ANY
    }

    data class IntLiteral(
        val where: Token,
        val value: Int): Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.intLiteral(this)
        override fun type() = ExprType.INT
    }

    data class BoolLiteral(
        val where: Token,
        val value: Boolean
    ): Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.boolLiteral(this)
        override fun type() = ExprType.BOOL
    }

    data class StringLiteral(
        val where: Token,
        val value: String): Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.stringLiteral(this)
        override fun type() = ExprType.STRING
    }

    data class CharLiteral(
        val where: Token,
        val value: Char
    ): Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.charLiteral(this)
        override fun type() = ExprType.CHAR
    }

    data class Alpha(
        val where: Token,
        val index: Int,
        val value: String) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.alpha(this)
        override fun type() = ExprType.ANY
    }

    data class Array(
        val where: Token,
        val elements: List<Expression>): Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.array(this)
        override fun type() = ExprType.ANY
    }

    data class Include(
        val names: List<String>): Expression(null) {

        override fun <R> accept(v: Visitor<R>): R = v.include(this)
        override fun type() = ExprType.NONE
    }

    data class NewObj(
        val where: Token,
        val name: String,
        val arguments: List<Expression>
    ): Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.new(this)
        override fun type() = ExprType.OBJECT
    }

    data class ThrowExpr(
        val error: Expression
    ): Expression(null) {

        override fun <R> accept(v: Visitor<R>) = v.throwExpr(this)
        override fun type() = ExprType.NONE
    }

    data class UnaryOperation(
        val where: Token,
        val operator: Type,
        val expr: Expression,
        val left: Boolean
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.unaryOperation(this)

        init { verify() }

        private fun verify() {
            val exprType = expr.type()
            if (left) {
                when (operator) {
                    Type.NEGATE -> {
                        if (exprType == ExprType.INT) return
                        where.error<String>("Expected Int expression to apply (-) Negate Operator")
                    }
                    Type.INCREMENT -> {
                        if (exprType == ExprType.INT) return
                        where.error<String>("Expected Int expression to apply (++) Increment Operator")
                    }
                    Type.DECREMENT -> {
                        if (exprType == ExprType.INT) return
                        where.error<String>("Expected Int expression to apply (--) Decrement Operator")
                    }
                    Type.NOT -> {
                        if (exprType == ExprType.BOOL) return
                        where.error<String>("Expected Boolean expression to apply (!) Not Operator")
                    }
                    else -> { }
                }
            } else {
                when (operator) {
                    Type.INCREMENT -> {
                        if (exprType == ExprType.INT) return
                        where.error<String>("Expected Int expression to apply (++) Increment Operator")
                    }
                    Type.DECREMENT -> {
                        if (exprType == ExprType.INT) return
                        where.error<String>("Expected Int expression to apply (--) Decrement Operator")
                    }
                    else -> { }
                }
            }
            where.error<String>("Unknown operator")
        }

        override fun type(): ExprType {
            val exprType = expr.type()
            if (left) {
                when {
                    operator == Type.NEGATE && exprType == ExprType.INT -> return ExprType.INT
                    operator == Type.INCREMENT && exprType == ExprType.INT -> return ExprType.INT
                    operator == Type.DECREMENT && exprType == ExprType.INT -> return ExprType.INT
                    operator == Type.NOT && exprType == ExprType.BOOL -> return ExprType.BOOL
                }
            } else {
                when {
                    operator == Type.INCREMENT && exprType == ExprType.INT -> return ExprType.INT
                    operator == Type.DECREMENT && exprType == ExprType.INT -> return ExprType.INT
                }
            }
            return ExprType.MISMATCHED
        }
    }

    data class BinaryOperation(
        val where: Token,
        val left: Expression,
        val right: Expression,
        val operator: Type) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.binaryOperation(this)

        override fun type(): ExprType {
            val leftType = left.type()
            val rightType = right.type()

            if (leftType == ExprType.INT && rightType == ExprType.INT) return ExprType.INT
            return ExprType.STRING
        }
    }

    data class DefinitionType(
        val name: String,
        val type: Type
    )

    data class ExplicitVariable(
        val where: Token,
        val mutable: Boolean,
        val definition: DefinitionType,
        val expr: Expression
    ) : Expression(where) {

        init { verify() }

        private fun verify() {
            val declaredType = ExprType.translate(definition.type)
            val valueType = expr.type()

            if (declaredType != ExprType.ANY && declaredType != valueType) {
                where.error<String>("Variable '${definition.name}' has type ${definition.type}" +
                        " but got ${ExprType.translateBack(valueType)}")
            }
        }

        override fun <R> accept(v: Visitor<R>) = v.variable(this)
        override fun type() = expr.type()
    }

    data class AutoVariable(
        val where: Token,
        val name: String,
        val expr: Expression
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.autoVariable(this)
        override fun type() = expr.type()
    }

    data class NativeCall(
        val where: Token,
        val type: Type,
        val arguments: List<Expression>,
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.nativeCall(this)

        // TODO:
        //  in future we need to document each native method call to it's
        //  actual signature
        override fun type() = ExprType.ANY
    }

    data class Scope(
        val where: Token,
        val expr: Expression,
        val imaginary: Boolean
    ): Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.scope(this)
        // TODO:
        //  in future we need to check this again, if we are doing it right
        override fun type() = ExprType.ANY
    }

    data class MethodCall(
        val where: Token,
        val fnExpr: FnElement,
        val arguments: List<Expression>,
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.methodCall(this)
        override fun type() = ExprType.translate(fnExpr.fnExpression!!.returnType)
    }

    data class ClassMethodCall(
        val where: Token,
        val static: Boolean,
        val obj: Expression,
        val method: String,
        val arguments: List<Expression>
    ): Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.classMethodCall(this)
        override fun type() = ExprType.ANY
    }

    data class ShadoInvoke(
        val where: Token,
        val expr: Expression,
        val arguments: List<Expression>
    ): Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.unitInvoke(this)
        override fun type() = ExprType.ANY
    }

    data class ExpressionList(
        val where: Token,
        val expressions: List<Expression>,
        var preserveState: Boolean = false,
    ) : Expression(where) {

        val size = expressions.size
        override fun <R> accept(v: Visitor<R>) = v.expressions(this)
        override fun type() = ExprType.ANY
    }

    data class ForEach(
        val where: Token,
        val name: String,
        val entity: Expression,
        val body: Expression,
    ): Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.forEach(this)
        override fun type() = ExprType.NONE
    }

    data class ForLoop(
        val where: Token,
        val initializer: Expression?,
        val conditional: Expression?,
        val operational: Expression?,
        val body: Expression,
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.forLoop(this)
        override fun type() = ExprType.NONE
    }

    data class Itr(
        val where: Token,
        val name: String,
        val from: Expression,
        val to: Expression,
        val by: Expression?,
        val body: Expression,
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.itr(this)
        override fun type() = ExprType.NONE
    }

    data class When(
        val where: Token,
        val expr: Expression,
        val matches: List<Pair<Expression, Expression>>,
        val defaultBranch: Expression,
    ): Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.whenExpr(this)
        // TODO:
        //  in future we have to analyze all the types, if all of the types are equal, return them
        //  or else we return type ANY
        override fun type() = ExprType.ANY
    }

    data class Until(
        val where: Token,
        val expression: Expression,
        val body: Expression,
        ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.until(this)
        override fun type() = ExprType.ANY
    }

    data class If(
        val where: Token,
        val condition: Expression,
        val thenBody: Expression,
        val elseBody: Expression? = null,
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.ifFunction(this)

        override fun type(): ExprType {
            val thenType = thenBody.type()
            if (elseBody == null) return thenType

            val elseType = elseBody.type()
            if (thenType == elseType) return thenType
            return ExprType.ANY
        }
    }

    data class Interruption(
        val where: Token,
        val operator: Type,
        val expr: Expression? = null)
        : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.interruption(this)
        override fun type() = expr?.type() ?: ExprType.NONE
    }

    data class Function(
        val where: Token,
        val name: String,
        val arguments: List<DefinitionType>,
        val returnType: Type,
        val body: Expression
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.function(this)
        override fun type() = ExprType.ANY
    }

    data class Shadow(
        val where: Token,
        val names: List<String>,
        val body: Expression
    ): Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.shado(this)
        override fun type() = ExprType.ANY
    }

    data class ElementAccess(
        val where: Token,
        val expr: Expression,
        val index: Expression
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.elementAccess(this)

        override fun type(): ExprType {
            val exprType = expr.type()
            if (exprType == ExprType.STRING) return ExprType.CHAR
            if (exprType == ExprType.INT) return ExprType.INT
            throw RuntimeException("Unknown element expr type $exprType")
        }
    }
}