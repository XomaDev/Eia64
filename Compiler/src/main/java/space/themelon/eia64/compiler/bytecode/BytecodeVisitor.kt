package space.themelon.eia64.compiler.bytecode

import space.themelon.eia64.compiler.bytecode.Bytecode.*
import space.themelon.eia64.compiler.Expression
import space.themelon.eia64.compiler.Nothing
import space.themelon.eia64.compiler.expressions.*
import space.themelon.eia64.compiler.syntax.Type

class BytecodeVisitor(
    private val dumper: Dumper
) : Expression.Visitor<Any?> {

    fun dump(node: ExpressionList) {
        node.accept(this)
        dumper.write(HALT)
    }

    override fun noneExpression() = Nothing.INSTANCE

    override fun nilLiteral(nil: NilLiteral) {
        dumper.write(NIL)
    }

    override fun intLiteral(literal: IntLiteral) {
        dumper.write(INT)
        dumper.writeInt32(literal.value)
    }

    override fun floatLiteral(literal: FloatLiteral): Any? {
        TODO("Not yet implemented")
    }

    override fun boolLiteral(literal: BoolLiteral) {
        dumper.write(BOOL)
        dumper.write(if (literal.value) 1 else 0)
    }

    override fun stringLiteral(literal: StringLiteral) {
        dumper.write(STRING)
        dumper.writeString(literal.value)
    }

    override fun charLiteral(literal: CharLiteral): Any? {
        TODO("Not yet implemented")
    }

    override fun typeLiteral(literal: TypeLiteral): Any? {
        TODO("Not yet implemented")
    }

    override fun alpha(alpha: Alpha): Any? {
        TODO("Not yet implemented")
    }

    override fun array(literal: ArrayLiteral): Any? {
        TODO("Not yet implemented")
    }

    override fun explicitArrayLiteral(arrayCreation: ExplicitArrayLiteral): Any? {
        TODO("Not yet implemented")
    }

    override fun arrayAllocation(arrayAllocation: ArrayAllocation): Any? {
        TODO("Not yet implemented")
    }

    override fun include(include: Include): Any? {
        TODO("Not yet implemented")
    }

    override fun new(new: NewObj): Any? {
        TODO("Not yet implemented")
    }

    override fun throwExpr(throwExpr: ThrowExpr): Any? {
        TODO("Not yet implemented")
    }

    override fun tryCatch(tryCatch: TryCatch): Any? {
        TODO("Not yet implemented")
    }

    override fun variable(variable: ExplicitVariable): Any? {
        TODO("Not yet implemented")
    }

    override fun autoVariable(autoVariable: AutoVariable): Any? {
        TODO("Not yet implemented")
    }

    override fun isStatement(isStatement: IsStatement): Any? {
        TODO("Not yet implemented")
    }

    override fun shado(shadow: Shadow): Any? {
        TODO("Not yet implemented")
    }

    override fun unaryOperation(expr: UnaryOperation): Any? {
        TODO("Not yet implemented")
    }

    override fun binaryOperation(expr: BinaryOperation) {
        expr.left.accept(this)
        expr.right.accept(this)
        dumper.write(when (val operator = expr.operator) {
            Type.PLUS -> ADD
            Type.NEGATE -> SUB
            Type.TIMES -> MUL
            Type.SLASH -> DIV
            else -> throw RuntimeException("Unknown binary operator $operator")
        })
    }

    override fun expressions(list: ExpressionList) {
        list.expressions.forEach { it.accept(this) }
    }

    override fun expressionBind(bind: ExpressionBind): Any? {
        TODO("Not yet implemented")
    }

    override fun nativeCall(call: NativeCall): Any? {
        TODO("Not yet implemented")
    }

    override fun cast(cast: Cast): Any? {
        TODO("Not yet implemented")
    }

    override fun scope(scope: Scope): Any? {
        TODO("Not yet implemented")
    }

    override fun methodCall(call: MethodCall): Any? {
        TODO("Not yet implemented")
    }

    override fun classPropertyAccess(propertyAccess: ForeignField): Any? {
        TODO("Not yet implemented")
    }

    override fun classMethodCall(call: ClassMethodCall): Any? {
        TODO("Not yet implemented")
    }

    override fun unitInvoke(shadoInvoke: ShadoInvoke): Any? {
        TODO("Not yet implemented")
    }

    override fun until(until: Until): Any? {
        TODO("Not yet implemented")
    }

    override fun itr(itr: Itr): Any? {
        TODO("Not yet implemented")
    }

    override fun whenExpr(whenExpr: When): Any? {
        TODO("Not yet implemented")
    }

    override fun forEach(forEach: ForEach): Any? {
        TODO("Not yet implemented")
    }

    override fun forLoop(forLoop: ForLoop): Any? {
        TODO("Not yet implemented")
    }

    override fun interruption(interruption: Interruption): Any? {
        TODO("Not yet implemented")
    }

    override fun ifFunction(ifExpr: IfStatement): Any? {
        TODO("Not yet implemented")
    }

    override fun function(function: FunctionExpr): Any? {
        TODO("Not yet implemented")
    }

    override fun arrayAccess(access: ArrayAccess): Any? {
        TODO("Not yet implemented")
    }
}