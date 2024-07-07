package space.themelon.eia64

import space.themelon.eia64.analysis.ExprType
import space.themelon.eia64.analysis.ExprType.Companion.typeEquals
import space.themelon.eia64.analysis.FunctionReference
import space.themelon.eia64.analysis.VariableReference
import space.themelon.eia64.analysis.VariableType
import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type
import space.themelon.eia64.syntax.Type.*

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
        val value: Any
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.genericLiteral(this)
        override fun type() = ExprType.ANY
    }

    data class IntLiteral(
        val where: Token,
        val value: Int
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.intLiteral(this)
        override fun type() = ExprType.INT
    }

    data class BoolLiteral(
        val where: Token,
        val value: Boolean
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.boolLiteral(this)
        override fun type() = ExprType.BOOL
    }

    data class StringLiteral(
        val where: Token,
        val value: String
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.stringLiteral(this)
        override fun type() = ExprType.STRING
    }

    data class CharLiteral(
        val where: Token,
        val value: Char
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.charLiteral(this)
        override fun type() = ExprType.CHAR
    }

    data class Alpha(
        val where: Token,
        val index: Int,
        val value: String,
        val vrType: VariableType? = null
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.alpha(this)
        override fun type(): ExprType {
            return vrType?.runtimeType ?: ExprType.ANY
        }
    }

    data class Array(
        val where: Token,
        val elements: List<Expression>
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.array(this)
        override fun type() = ExprType.ANY
    }

    data class Include(
        val names: List<String>
    ) : Expression(null) {

        override fun <R> accept(v: Visitor<R>): R = v.include(this)
        override fun type() = ExprType.NONE
    }

    data class NewObj(
        val where: Token,
        val name: String,
        val arguments: List<Expression>
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.new(this)
        override fun type() = ExprType.OBJECT
    }

    data class ThrowExpr(
        val error: Expression
    ) : Expression(null) {

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

        init {
            verify()
        }

        private fun verify() {
            val exprType = expr.type()
            if (left) {
                when (operator) {
                    NEGATE -> {
                        if (typeEquals(exprType, ExprType.INT)) return
                        where.error<String>("Expected Int expression to apply (-) Negate Operator, got $exprType")
                    }

                    INCREMENT -> {
                        if (typeEquals(exprType, ExprType.INT)) return
                        where.error<String>("Expected Int expression to apply (++) Increment Operator, got $exprType")
                    }

                    DECREMENT -> {
                        if (typeEquals(exprType, ExprType.INT)) return
                        where.error<String>("Expected Int expression to apply (--) Decrement Operator, got $exprType")
                    }

                    NOT -> {
                        println("expr = $expr")
                        if (typeEquals(exprType, ExprType.BOOL)) return
                        where.error<String>("Expected Boolean expression to apply (!) Not Operator, got $exprType")
                    }

                    else -> {}
                }
            } else {
                when (operator) {
                    INCREMENT -> {
                        if (typeEquals(exprType, ExprType.INT)) return
                        where.error<String>("Expected Int expression to apply (++) Increment Operator, got $exprType")
                    }

                    DECREMENT -> {
                        if (typeEquals(exprType, ExprType.INT)) return
                        where.error<String>("Expected Int expression to apply (--) Decrement Operator, got $exprType")
                    }

                    else -> {}
                }
            }
            where.error<String>("Unknown operator")
        }

        override fun type(): ExprType {
            val exprType = expr.type()
            if (left) {
                when {
                    operator == NEGATE && exprType == ExprType.INT -> return ExprType.INT
                    operator == INCREMENT && exprType == ExprType.INT -> return ExprType.INT
                    operator == DECREMENT && exprType == ExprType.INT -> return ExprType.INT
                    operator == NOT && exprType == ExprType.BOOL -> return ExprType.BOOL
                }
            } else {
                when {
                    operator == INCREMENT && exprType == ExprType.INT -> return ExprType.INT
                    operator == DECREMENT && exprType == ExprType.INT -> return ExprType.INT
                }
            }
            return ExprType.MISMATCHED
        }
    }

    data class BinaryOperation(
        val where: Token,
        val left: Expression,
        val right: Expression,
        val operator: Type
    ) : Expression(where) {

        init {
            verify()
        }

        override fun <R> accept(v: Visitor<R>) = v.binaryOperation(this)

        private fun verify() {
            println("left = $left, right = $right, operator = $operator")
            val leftType = left.type()
            val rightType = right.type()

            when (operator) {
                NEGATE,
                TIMES,
                SLASH,
                BITWISE_AND,
                BITWISE_OR,
                GREATER_THAN,
                LESSER_THAN,
                GREATER_THAN_EQUALS,
                LESSER_THAN_EQUALS,
                DEDUCTIVE_ASSIGNMENT,
                MULTIPLICATIVE_ASSIGNMENT,
                DIVIDIVE_ASSIGNMENT,
                POWER -> {
                    if (leftType == ExprType.INT && rightType == ExprType.INT) return
                    where.error<String>("Cannot apply to a non Int expressions")
                }
                LOGICAL_AND, LOGICAL_OR -> {
                    if (leftType == ExprType.BOOL && rightType == ExprType.BOOL) return
                    where.error<String>("Cannot apply to a non Bool expressions")
                }

                ADDITIVE_ASSIGNMENT -> {
                    if (leftType == ExprType.STRING && (rightType == ExprType.STRING || rightType == ExprType.CHAR)
                        || leftType == ExprType.INT && rightType == ExprType.INT) return
                    where.error<String>("Cannot apply operator to a non String or Int")
                }
                ASSIGNMENT -> left.type()
                EQUALS, NOT_EQUALS, PLUS -> { }
                else -> where.error("Undocumented binary operator")
            }
        }

        override fun type(): ExprType {
            val leftType = left.type()
            val rightType = right.type()

            return when (operator) {
                PLUS -> {
                    if (leftType == ExprType.INT && rightType == ExprType.INT) ExprType.INT
                    else ExprType.STRING
                }

                NEGATE, TIMES, SLASH, BITWISE_AND, BITWISE_OR -> ExprType.INT

                EQUALS,
                NOT_EQUALS,
                LOGICAL_AND,
                LOGICAL_OR,
                GREATER_THAN,
                LESSER_THAN,
                GREATER_THAN_EQUALS,
                LESSER_THAN_EQUALS -> ExprType.BOOL

                ASSIGNMENT -> left.type() // TODO: confirm this in future
                ADDITIVE_ASSIGNMENT -> if (left.type() == ExprType.STRING) ExprType.STRING else ExprType.INT
                DEDUCTIVE_ASSIGNMENT, MULTIPLICATIVE_ASSIGNMENT, DIVIDIVE_ASSIGNMENT, POWER -> ExprType.INT

                else -> throw RuntimeException("Unknown operator $operator")
            }
        }
    }

    data class DefinitionType(
        val name: String,
        val type: Type,
        val className: String? = null
    )

    data class ExplicitVariable(
        val where: Token,
        val mutable: Boolean,
        val definition: DefinitionType,
        val expr: Expression
    ) : Expression(where) {

        init {
            verify()
        }

        private fun verify() {
            val declaredType = ExprType.translate(definition.type)
            val valueType = expr.type()

            if (declaredType != ExprType.ANY && declaredType != valueType) {
                where.error<String>(
                    "Variable '${definition.name}' has type ${definition.type}" +
                            " but got ${ExprType.translateBack(valueType)}"
                )
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

        override fun type() = when (type) {
            PRINT, PRINTLN -> ExprType.INT
            READ, READLN -> ExprType.STRING
            SLEEP -> ExprType.INT
            LEN -> ExprType.INT
            FORMAT -> ExprType.STRING
            INT_CAST -> ExprType.INT
            STRING_CAST -> ExprType.STRING
            BOOL_CAST -> ExprType.BOOL
            TYPE -> ExprType.STRING
            INCLUDE -> ExprType.BOOL
            // TODO: we need to figure out a better way
            COPY -> arguments[0].type()
            ARRAYOF, ARRALLOC -> ExprType.ARRAY
            TIME -> ExprType.INT
            RAND -> ExprType.INT
            EXIT -> ExprType.INT
            else -> throw RuntimeException("Unknown native call type $type")
        }
    }

    data class Scope(
        val expr: Expression,
        val imaginary: Boolean
    ) : Expression(null) {

        override fun <R> accept(v: Visitor<R>) = v.scope(this)

        // TODO:
        //  in future we need to check this again, if we are doing it right
        override fun type() = ExprType.ANY
    }

    data class MethodCall(
        val where: Token,
        val fnExpr: FunctionReference,
        val arguments: List<Expression>,
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.methodCall(this)
        override fun type() = ExprType.translate(fnExpr.returnType)
    }

    data class ClassMethodCall(
        val where: Token,
        val static: Boolean,
        val obj: Expression,
        val method: String,
        val arguments: List<Expression>,
        val returnType: ExprType
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.classMethodCall(this)
        override fun type() = returnType
    }

    data class ShadoInvoke(
        val where: Token,
        val expr: Expression,
        val arguments: List<Expression>
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.unitInvoke(this)
        override fun type() = ExprType.ANY
    }

    data class ExpressionList(
        val expressions: List<Expression>,
        var preserveState: Boolean = false,
    ) : Expression(null) {

        val size = expressions.size
        override fun <R> accept(v: Visitor<R>) = v.expressions(this)
        override fun type() = ExprType.ANY
    }

    data class ForEach(
        val where: Token,
        val name: String,
        val entity: Expression,
        val body: Expression,
    ) : Expression(where) {

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
    ) : Expression(where) {

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
        val expr: Expression? = null
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.interruption(this)
        override fun type() = expr?.type() ?: ExprType.NONE
    }

    data class Function(
        val name: String,
        val arguments: List<DefinitionType>,
        val returnType: Type,
        val body: Expression
    ) : Expression(null) {

        override fun <R> accept(v: Visitor<R>) = v.function(this)
        override fun type() = ExprType.NONE // this is just a pure declration
    }

    data class Shadow(
        val names: List<String>,
        val body: Expression
    ) : Expression() {

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
            if (exprType == ExprType.ARRAY) return ExprType.ANY
            throw RuntimeException("Unknown element expr type $exprType")
        }
    }
}