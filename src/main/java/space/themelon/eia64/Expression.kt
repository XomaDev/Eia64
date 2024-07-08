package space.themelon.eia64

import space.themelon.eia64.analysis.*
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
        fun cast(cast: Cast): R
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
    abstract fun signature(): ExpressionSignature

    // for internal evaluation use
    data class GenericLiteral(
        val where: Token,
        val value: Any
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.genericLiteral(this)
        override fun signature() = ExpressionSignature(ExpressionType.ANY)
    }

    data class IntLiteral(
        val where: Token,
        val value: Int
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.intLiteral(this)
        override fun signature() = ExpressionSignature(ExpressionType.INT, VariableMetadata(ExpressionType.INT, "eint"))
    }

    data class BoolLiteral(
        val where: Token,
        val value: Boolean
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.boolLiteral(this)
        override fun signature() = ExpressionSignature(ExpressionType.BOOL)
    }

    data class StringLiteral(
        val where: Token,
        val value: String
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.stringLiteral(this)
        override fun signature() = ExpressionSignature(ExpressionType.STRING, VariableMetadata(ExpressionType.STRING, "string"))
    }

    data class CharLiteral(
        val where: Token,
        val value: Char
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.charLiteral(this)
        override fun signature() = ExpressionSignature(ExpressionType.CHAR)
    }

    data class Alpha(
        val where: Token,
        val index: Int,
        val value: String,
        val vrType: VariableMetadata? = null
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.alpha(this)
        override fun signature(): ExpressionSignature {
            if (vrType == null) return ExpressionSignature(ExpressionType.ANY)
            println("alpha $value vr type = $vrType")
            return ExpressionSignature(vrType.runtimeType, vrType)
        }
    }

    data class Array(
        val where: Token,
        val elements: List<Expression>
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.array(this)
        override fun signature() = ExpressionSignature(ExpressionType.ARRAY, VariableMetadata(ExpressionType.ARRAY, "array"))
    }

    data class Include(
        val names: List<String>
    ) : Expression(null) {

        override fun <R> accept(v: Visitor<R>): R = v.include(this)
        override fun signature() = ExpressionSignature(ExpressionType.NONE)
    }

    data class NewObj(
        val where: Token,
        val name: String,
        val arguments: List<Expression>
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.new(this)
        override fun signature() = ExpressionSignature(ExpressionType.OBJECT, VariableMetadata(ExpressionType.OBJECT, name))
    }

    data class ThrowExpr(
        val error: Expression
    ) : Expression(null) {

        override fun <R> accept(v: Visitor<R>) = v.throwExpr(this)
        override fun signature() = ExpressionSignature(ExpressionType.NONE)
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
            val exprType = expr.signature()
            if (left) {
                when (operator) {
                    NEGATE -> {
                        if (exprType.type == ExpressionType.INT) return
                        where.error<String>("Expected Int expression to apply (-) Negate Operator, got $exprType")
                    }

                    INCREMENT -> {
                        if (exprType.type == ExpressionType.INT) return
                        where.error<String>("Expected Int expression to apply (++) Increment Operator, got $exprType")
                    }

                    DECREMENT -> {
                        if (exprType.type == ExpressionType.INT) return
                        where.error<String>("Expected Int expression to apply (--) Decrement Operator, got $exprType")
                    }

                    NOT -> {
                        if (exprType.type == ExpressionType.BOOL) return
                        where.error<String>("Expected Boolean expression to apply (!) Not Operator, got $exprType")
                    }

                    else -> {}
                }
            } else {
                when (operator) {
                    INCREMENT -> {
                        if (exprType.type == ExpressionType.INT) return
                        where.error<String>("Expected Int expression to apply (++) Increment Operator, got $exprType")
                    }

                    DECREMENT -> {
                        if (exprType.type == ExpressionType.INT) return
                        where.error<String>("Expected Int expression to apply (--) Decrement Operator, got $exprType")
                    }

                    else -> {}
                }
            }
            where.error<String>("Unknown operator")
        }

        override fun signature(): ExpressionSignature {
            val exprType = expr.signature()
            return ExpressionSignature(if (left) {
                when {
                    operator == NEGATE && exprType.type == ExpressionType.INT -> ExpressionType.INT
                    operator == INCREMENT && exprType.type == ExpressionType.INT -> ExpressionType.INT
                    operator == DECREMENT && exprType.type == ExpressionType.INT -> ExpressionType.INT
                    operator == NOT && exprType.type == ExpressionType.BOOL -> ExpressionType.BOOL
                    else -> ExpressionType.NONE
                }
            } else {
                when {
                    operator == INCREMENT && exprType.type == ExpressionType.INT -> ExpressionType.INT
                    operator == DECREMENT && exprType.type == ExpressionType.INT -> ExpressionType.INT
                    else -> ExpressionType.NONE
                }
            })
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
            val leftType = left.signature()
            val rightType = right.signature()

            when (operator) {
                NEGATE,
                TIMES,
                SLASH,
                BITWISE_AND,
                BITWISE_OR,
                RIGHT_DIAMOND,
                LEFT_DIAMOND,
                GREATER_THAN_EQUALS,
                LESSER_THAN_EQUALS,
                DEDUCTIVE_ASSIGNMENT,
                MULTIPLICATIVE_ASSIGNMENT,
                DIVIDIVE_ASSIGNMENT,
                POWER -> {
                    if (leftType.type == ExpressionType.INT && rightType.type == ExpressionType.INT) return
                    where.error<String>("Cannot apply to a non Int expressions")
                }
                LOGICAL_AND, LOGICAL_OR -> {
                    println("left=${leftType.type}")
                    println("left=${rightType.type}")
                    if (leftType.type == ExpressionType.BOOL && rightType.type == ExpressionType.BOOL) return
                    where.error<String>("Cannot apply to a non Bool expressions")
                }

                ADDITIVE_ASSIGNMENT -> {
                    if (leftType.type == ExpressionType.STRING
                        && (rightType.type == ExpressionType.STRING || rightType.type == ExpressionType.CHAR)
                        || leftType.type == ExpressionType.INT && rightType.type == ExpressionType.INT) return
                    where.error<String>("Cannot apply operator to a non String or Int")
                }
                ASSIGNMENT -> left.signature()
                EQUALS, NOT_EQUALS, PLUS -> { }
                else -> where.error("Undocumented binary operator")
            }
        }

        override fun signature(): ExpressionSignature {
            val leftType = left.signature().type
            val rightType = right.signature().type

            return ExpressionSignature(when (operator) {
                PLUS -> {
                    if (leftType == ExpressionType.INT && rightType == ExpressionType.INT) ExpressionType.INT
                    else ExpressionType.STRING
                }

                NEGATE, TIMES, SLASH, BITWISE_AND, BITWISE_OR -> ExpressionType.INT

                EQUALS,
                NOT_EQUALS,
                LOGICAL_AND,
                LOGICAL_OR,
                RIGHT_DIAMOND,
                LEFT_DIAMOND,
                GREATER_THAN_EQUALS,
                LESSER_THAN_EQUALS -> ExpressionType.BOOL

                ASSIGNMENT -> left.signature().type // TODO: confirm this in future
                ADDITIVE_ASSIGNMENT -> if (left.signature().type == ExpressionType.STRING) ExpressionType.STRING else ExpressionType.INT
                DEDUCTIVE_ASSIGNMENT, MULTIPLICATIVE_ASSIGNMENT, DIVIDIVE_ASSIGNMENT, POWER -> ExpressionType.INT

                else -> throw RuntimeException("Unknown operator $operator")
            })
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
            val declaredType = ExpressionType.translate(definition.type)
            val valueType = expr.signature()

            // TODO:
            //   here we may require deep matching
            if (declaredType != ExpressionType.ANY && declaredType != valueType.type) {
                where.error<String>(
                    "Variable '${definition.name}' has type ${definition.type}" +
                            " but got ${ExpressionType.translateBack(valueType.type)}"
                )
            }
        }

        override fun <R> accept(v: Visitor<R>) = v.variable(this)
        override fun signature() = expr.signature()
    }

    data class AutoVariable(
        val where: Token,
        val name: String,
        val expr: Expression
    ) : Expression(where) {

        init {
            signature()
        }

        override fun <R> accept(v: Visitor<R>) = v.autoVariable(this)
        override fun signature(): ExpressionSignature {
            val signature = expr.signature()
            println("SignatureX: $signature")
            return signature
        }
    }

    data class Cast(
        val where: Token,
        val expr: Expression,
        val metadata: VariableMetadata
    ): Expression(where) {
        override fun <R> accept(v: Visitor<R>) = expr.accept(v) // do a direct bypass, this isn't required at runtime
        override fun signature() = ExpressionSignature(metadata.runtimeType, metadata)
    }

    data class NativeCall(
        val where: Token,
        val type: Type,
        val arguments: List<Expression>,
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.nativeCall(this)

        override fun signature() = ExpressionSignature(when (type) {
            PRINT, PRINTLN -> ExpressionType.INT
            READ, READLN -> ExpressionType.STRING
            SLEEP -> ExpressionType.INT
            LEN -> ExpressionType.INT
            FORMAT -> ExpressionType.STRING
            INT_CAST -> ExpressionType.INT
            STRING_CAST -> ExpressionType.STRING
            BOOL_CAST -> ExpressionType.BOOL
            TYPE -> ExpressionType.STRING
            INCLUDE -> ExpressionType.BOOL
            // TODO: we need to figure out a better way
            COPY -> arguments[0].signature().type
            ARRAYOF, ARRALLOC -> ExpressionType.ARRAY
            TIME -> ExpressionType.INT
            RAND -> ExpressionType.INT
            EXIT -> ExpressionType.INT
            else -> throw RuntimeException("Unknown native call type $type")
        })
    }

    data class Scope(
        val expr: Expression,
        val imaginary: Boolean
    ) : Expression(null) {

        override fun <R> accept(v: Visitor<R>) = v.scope(this)

        // TODO:
        //  in future we need to check this again, if we are doing it right
        override fun signature() = expr.signature()
    }

    data class MethodCall(
        val name: Token,
        val functionReference: FunctionReference,
        val arguments: List<Expression>,
    ) : Expression(name) {

        init {
            verify()
        }

        private fun verify() {
            val argSignature = functionReference.arguments
            val expectedArgs = argSignature.size
            val gotArgs = arguments.size

            if (expectedArgs != gotArgs) {
                name.error<String>("Function $name() expected $expectedArgs args but got $gotArgs args")
                return
            }

            val argumentItr = arguments.iterator()
            val signatureItr = argSignature.iterator()

            while (argumentItr.hasNext()) {
                // TODO:
                //  in future, we need to also check the Object type (there could be multiple object types)

                val expected = signatureItr.next()
                val argName = expected.name

                val gotSignature = argumentItr.next().signature()

                val expectedMetadata = expected.metadata
                val gotMetadata = gotSignature.metadata

                if (expectedMetadata.runtimeType != gotMetadata!!.runtimeType) {
                    name.error<String>("Arg '$argName' in function $name() expected type ${expectedMetadata.runtimeType} but got $gotSignature")
                    return
                }

                if (expectedMetadata.getModule() != gotMetadata.getModule()) {
                    name.error<String>("Arg '$argName' in function $name() expected type" +
                            " '${expectedMetadata.getModule()}' but got '${gotMetadata.getModule()}'")
                    return
                }
            }
        }

        override fun <R> accept(v: Visitor<R>) = v.methodCall(this)

        override fun signature(): ExpressionSignature {
            val returnType = ExpressionType.translate(functionReference.returnType)
            return ExpressionSignature(returnType, VariableMetadata(returnType))
        }
    }

    data class ClassMethodCall(
        val where: Token,
        val static: Boolean,
        val obj: Expression,
        val method: String,
        val arguments: List<Expression>,
        val returnType: ExpressionType
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.classMethodCall(this)
        override fun signature() = ExpressionSignature(returnType)
    }

    data class ShadoInvoke(
        val where: Token,
        val expr: Expression,
        val arguments: List<Expression>
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.unitInvoke(this)
        override fun signature() = ExpressionSignature(ExpressionType.ANY)
    }

    data class ExpressionList(
        val expressions: List<Expression>,
        var preserveState: Boolean = false,
    ) : Expression(null) {

        val size = expressions.size
        override fun <R> accept(v: Visitor<R>) = v.expressions(this)
        override fun signature() = ExpressionSignature(ExpressionType.ANY)
    }

    data class ForEach(
        val where: Token,
        val name: String,
        val entity: Expression,
        val body: Expression,
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.forEach(this)
        override fun signature() = ExpressionSignature(ExpressionType.NONE)
    }

    data class ForLoop(
        val where: Token,
        val initializer: Expression?,
        val conditional: Expression?,
        val operational: Expression?,
        val body: Expression,
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.forLoop(this)
        override fun signature() = ExpressionSignature(ExpressionType.NONE)
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

    data class Until(
        val where: Token,
        val expression: Expression,
        val body: Expression,
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.until(this)
        override fun signature() = ExpressionSignature(ExpressionType.ANY)
    }

    data class If(
        val where: Token,
        val condition: Expression,
        val thenBody: Expression,
        val elseBody: Expression? = null,
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.ifFunction(this)

        override fun signature(): ExpressionSignature {
            val thenType = thenBody.signature()
            if (elseBody == null) return thenType

            val elseType = elseBody.signature()
            if (thenType == elseType) return thenType
            return ExpressionSignature(ExpressionType.ANY)
        }
    }

    data class Interruption(
        val where: Token,
        val operator: Type,
        val expr: Expression? = null
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.interruption(this)
        override fun signature() = expr?.signature() ?: ExpressionSignature(ExpressionType.NONE)
    }

    data class Function(
        val name: String,
        val arguments: List<ValueDefinition>,
        val returnType: Type,
        val body: Expression
    ) : Expression(null) {

        override fun <R> accept(v: Visitor<R>) = v.function(this)
        override fun signature() = ExpressionSignature(ExpressionType.NONE) // this is just a pure declration
    }

    data class Shadow(
        val names: List<String>,
        val body: Expression
    ) : Expression() {

        override fun <R> accept(v: Visitor<R>) = v.shado(this)
        override fun signature() = ExpressionSignature(ExpressionType.ANY)
    }

    data class ElementAccess(
        val where: Token,
        val expr: Expression,
        val index: Expression
    ) : Expression(where) {

        override fun <R> accept(v: Visitor<R>) = v.elementAccess(this)

        override fun signature(): ExpressionSignature {
            val exprType = expr.signature().type
            if (exprType == ExpressionType.STRING) return ExpressionSignature(ExpressionType.CHAR)
            if (exprType == ExpressionType.ARRAY) return ExpressionSignature(ExpressionType.ANY)
            throw RuntimeException("Unknown element expr type $exprType")
        }
    }
}