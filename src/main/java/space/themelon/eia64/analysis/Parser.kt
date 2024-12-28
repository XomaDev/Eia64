package space.themelon.eia64.analysis

import space.themelon.eia64.Expression
import space.themelon.eia64.expressions.*
import space.themelon.eia64.expressions.ArrayLiteral
import space.themelon.eia64.mirror.Mirror
import space.themelon.eia64.runtime.Environment
import space.themelon.eia64.signatures.*
import space.themelon.eia64.signatures.Matching.matches
import space.themelon.eia64.syntax.Flag
import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type
import java.util.StringJoiner

class Parser(
    private val environment: Environment,
) {

    private var manager = ScopeManager()

    private lateinit var tokens: List<Token>
    private var index = 0
    private var size = 0

    lateinit var parsed: ExpressionList

    fun reset() {
        manager = ScopeManager()
    }

    fun parse(tokens: List<Token>): ExpressionList {
        index = 0
        size = tokens.size
        this.tokens = tokens

        val expressions = ArrayList<Expression>()
        parseSkeleton()
        while (!isEOF()) expressions.add(statement())
        if (Environment.DEBUG) expressions.forEach { println(it) }
        parsed = ExpressionList(expressions)
        return parsed
    }

    // make sure to update canParseNext() when we add stuff here!
    private fun statement(): Expression {
        val token = next()
        if (token.flags.isNotEmpty()) {
            when (token.flags[0]) {
                Flag.LOOP -> return loop(token)
                Flag.V_KEYWORD -> return variableDeclaration(token)
                Flag.INTERRUPTION -> return interruption(token)
                else -> {}
            }
        }
        return when (token.type) {
            Type.IF -> ifSmt(token)
            Type.FUN -> funSmt(token.type)
            Type.IMPORT -> importStatement(token)
            Type.THROW -> throwStatement(token)
            else -> {
                back()
                parseExpr(0)
            }
        }
    }

    private fun canParseNext(): Boolean {
        val token = peek()
        if (token.flags.isNotEmpty())
            token.flags[0].let {
                if (it == Flag.LOOP
                    || it == Flag.V_KEYWORD
                    || it == Flag.INTERRUPTION
                )
                    return true
            }
        return when (token.type) {
            Type.IF,
            Type.FUN,
            Type.THROW -> true

            else -> false
        }
    }

    private fun parseSkeleton() {
        // We'll be bumping Indexes, so save it to set back later
        val originalIndex = index

        var curlyBracesCount = 0

        fun handleFn(visible: Boolean) {
            // A function, now we parse its signature!
            val reference = functionOutline(visible)
            // Predefine all the outlines!
            manager.defineSemiFn(reference.name, reference)
        }

        while (!isEOF()) {
            val token = next()
            when (val type = token.type) {
                Type.OPEN_CURLY -> curlyBracesCount++
                Type.CLOSE_CURLY -> {
                    if (curlyBracesCount == 0) break
                    else curlyBracesCount--
                }

                Type.FUN -> if (curlyBracesCount == 0) handleFn(false)
                Type.PUBLIC, Type.PRIVATE -> {
                    if (curlyBracesCount == 0 && isNext(Type.FUN)) {
                        skip()
                        handleFn(type == Type.PUBLIC)
                    }
                }

                else -> {}
            }
        }

        index = originalIndex
    }

    private fun throwStatement(token: Token) = ThrowExpr(token, statement())

    private fun importStatement(token: Token): Expression {
        val clazz = Mirror.lookupClass(token.data as String)
        val pkgName = clazz.name
        val simpleName = pkgName.substring(pkgName.lastIndexOf('.') + 1)
        environment.classes += simpleName to clazz
        return NoneExpression()
    }

    private fun parseNextInBrace(): Expression {
        // we do it this way, just calling parseNext() would work, but it increases code flow
        // which may make it harder to debug the Parser.

        eat(Type.OPEN_CURVE)
        val expr = statement()
        eat(Type.CLOSE_CURVE)
        return expr
    }


    private fun loop(where: Token): Expression {
        when (where.type) {
            Type.UNTIL -> {
                val expr = between(Type.OPEN_CURVE, Type.CLOSE_CURVE) { statement() }
                // Scope: Automatic
                val body = manager.iterativeScope { smtOrBody() }
                return Until(where, expr, body)
            }

            Type.FOR -> {
                // we cannot expose initializers outside the for loop
                eat(Type.OPEN_CURVE)
                return if (isNext(Type.ALPHA)) forEach(where) else forVariableLoop(where)
            }

            Type.EACH -> {
                eat(Type.OPEN_CURVE)
                val iName = eat(Type.ALPHA).data as String
                eat(Type.COLON)

                val from = statement()
                eat(Type.TO)
                val to = statement()

                var by: Expression? = null
                if (isNext(Type.BY)) {
                    index++
                    by = statement()
                }
                eat(Type.CLOSE_CURVE)
                manager.enterScope()
                manager.defineVariable(iName, SignatureConstants.INT)
                // Manual Scopped!
                val body = manager.iterativeScope { manualSmtBody() }
                manager.leaveScope()
                return Itr(where, iName, from, to, by, body)
            }

            else -> return where.error("Unknown loop type symbol")
        }
    }


    private fun forLoop(where: Token): Expression {
        // we cannot expose initializers outside the for loop
        eat(Type.OPEN_CURVE)
        val expression = if (isNext(Type.ALPHA)) forEach(where) else forVariableLoop(where)
        return expression
    }

    private fun forEach(where: Token): ForEach {
        val iName = eat(Type.ALPHA).data as String
        eat(Type.IN)
        val entity = statement()
        eat(Type.CLOSE_CURVE)

        val elementSignature = when (entity.sig()) {
            SignatureConstants.LIST -> SignatureConstants.ANY
            SignatureConstants.STRING -> SignatureConstants.CHAR

            else -> {
                where.error<String>("Unknown non iterable element for '$iName'")
                throw RuntimeException()
            }
        }

        manager.enterScope()
        manager.defineVariable(iName, elementSignature)
        // Manual Scopped!
        val body = manager.iterativeScope { this.manualSmtBody() }
        manager.leaveScope()
        return ForEach(where, iName, entity, body)
    }

    private fun forVariableLoop(
        where: Token,
    ): ForLoop {
        manager.enterScope()
        val initializer = if (isNext(Type.SEMI_COLON)) null else statement()
        eat(Type.SEMI_COLON)
        val conditional = if (isNext(Type.SEMI_COLON)) null else statement()
        eat(Type.SEMI_COLON)
        val operational = if (isNext(Type.CLOSE_CURVE)) null else statement()
        eat(Type.CLOSE_CURVE)
        // double layer scope wrapping
        // Scope: Automatic
        val body = manager.iterativeScope { smtOrBody() }
        manager.leaveScope()
        return ForLoop(
            where,
            initializer,
            conditional,
            operational,
            body
        )
    }

    private fun interruption(token: Token): Interruption {
        // checks if `continue and `break` statement are allowed
        if ((token.type == Type.CONTINUE || token.type == Type.BREAK) && !manager.isIterativeScope) {
            val type = if (token.type == Type.CONTINUE) "Continue" else "Break"
            token.error<String>("$type statement is not allowed here") // End of Execution
            throw RuntimeException()
        }
        return Interruption(
            token,
            token.type,
            when (token.type) {
                Type.RETURN -> {
                    val expectedSignature = manager.getPromisedSignature
                    if (expectedSignature == SignatureConstants.NONE) {
                        null
                    } else {
                        val expr = statement()
                        val gotSignature = expr.sig()
                        if (!matches(expectedSignature, gotSignature)) {
                            token.error<String>("Was expecting return type of $expectedSignature but got $gotSignature")
                            throw RuntimeException()
                        }
                        expr
                    }
                }

                Type.USE -> statement()
                else -> null
            }
        )
    }

    private fun functionOutline(public: Boolean): FunctionReference {
        val where = next()
        val name = readAlpha(where)

        eat(Type.OPEN_CURVE)
        val requiredArgs = mutableListOf<Pair<String, Signature>>()
        while (!isEOF() && peek().type != Type.CLOSE_CURVE) {
            val parameterName = readAlpha()
            eat(Type.COLON)
            val signature = readSignature(next())

            requiredArgs += parameterName to signature
            if (!isNext(Type.COMMA)) break
            skip()
        }
        eat(Type.CLOSE_CURVE)

        val isVoid: Boolean
        val returnSignature = if (isNext(Type.COLON)) {
            skip()
            isVoid = false
            readSignature(next())
        } else {
            isVoid = true
            SignatureConstants.UNIT
        }

        return FunctionReference(
            where,
            name,
            null,
            requiredArgs,
            requiredArgs.size,
            returnSignature,
            isVoid,
            public,
            index
        )
    }

    private fun funSmt(type: Type): FunctionExpr {
        val reference = manager.readFnOutline()
        index = reference.tokenIndex
        manager.enterScope()
        reference.parameters.forEach { manager.defineVariable(it.first, it.second) }

        val body: Expression = if (isNext(Type.ASSIGNMENT)) {
            index++
            statement()
        } else {
            manager.expectReturn(reference.returnSignature) {
                expressions()
            }
        }
        manager.leaveScope()

        val fnExpr = FunctionExpr(
            reference.where,
            reference.name,
            reference.parameters,
            reference.isVoid,
            reference.returnSignature,
            body
        )
        reference.fnExpression = fnExpr
        return fnExpr
    }

    private fun ifSmt(where: Token): IfStatement {
        val condition = between(Type.OPEN_CURVE, Type.CLOSE_CURVE) { statement() }
        val thenBody = smtOrBody()

        if (isEOF() || !isNext(Type.ELSE)) return IfStatement(where, condition, thenBody, NoneExpression.INSTANCE)
        skip()
        val elseBody = if (isNext(Type.IF)) ifSmt(next()) else smtOrBody()
        return IfStatement(where, condition, thenBody, elseBody)
    }

    // automatic scope operator
    private fun smtOrBody(): Scope {
        manager.enterScope()
        if (isNext(Type.OPEN_CURLY)) {
            val body = Scope(expressions(), manager.leaveScope())
            return body
        }
        return Scope(statement(), manager.leaveScope())
    }

    private fun manualSmtBody() = if (isNext(Type.OPEN_CURLY)) expressions() else statement()

    private fun expressions(): Expression {
        eat(Type.OPEN_CURLY)
        parseSkeleton()
        val expressions = ArrayList<Expression>()
        while (notEOF() && !consume(Type.CLOSE_CURLY))
            expressions.add(statement())
        return ExpressionList(expressions)
    }

    private fun variableDeclaration(where: Token): Expression {
        val expressions = mutableListOf<Expression>()
        do {
            // read minimum one declaration
            expressions += readVariableDeclaration(where)
            //println("Iteration: " + expressions.last())
        } while (isNext(Type.COMMA).also { if (it) next() })

        if (expressions.size == 1) return expressions.first()
        return ExpressionBind(expressions)
    }

    private fun readVariableDeclaration(
        where: Token,
    ): Expression {
        val name = eat(Type.ALPHA).data as String

        val expr: Expression
        val signature: Signature

        if (!isNext(Type.COLON)) {
            val assignmentExpr = readVariableExpr()
            signature = assignmentExpr.sig()
            expr = Variable(where, name, assignmentExpr)
        } else {
            index++
            signature = readSignature(next())
            expr = Variable(
                where,
                name,
                readVariableExpr(),
                signature
            )
        }
        manager.defineVariable(name, signature)
        return expr
    }

    private fun readSignature(token: Token): Signature {
        if (token.type != Type.ALPHA) {
            token.error<String>("Expected a valid signature type")
            // end of exec
        }
        return when (val name = token.data as String) {
            "Nil" -> SignatureConstants.NIL
            "Int" -> SignatureConstants.INT
            "Float" -> SignatureConstants.FLOAT
            "Long" -> SignatureConstants.LONG
            "Bool" -> SignatureConstants.BOOL
            "String" -> SignatureConstants.STRING
            "Char" -> SignatureConstants.CHAR
            "Any" -> SignatureConstants.ANY
            "Array" -> SignatureConstants.ARRAY
            "Unit" -> SignatureConstants.UNIT
            "Java" -> SignatureConstants.JAVA
            else -> {
                environment.classes[name]?.let { return ClassSignature(it) }
                throw IllegalArgumentException("Unknown signature $name")
            }
        }
    }


    private fun readVariableExpr(): Expression {
        if (consume(Type.ASSIGNMENT)) return statement()
        return peek().error("Unexpected variable expression")
    }

    private fun parseExpr(minPrecedence: Int): Expression {
        // this parses a full expressions, until it's done!
        var left = element()
        if (!isEOF() && peek().hasFlag(Flag.POSSIBLE_RIGHT_UNARY)) {
            val where = next()
            left = UnaryOperation(where, where.type, left, false)
        }
        while (!isEOF()) {
            val opToken = peek()
            if (!opToken.hasFlag(Flag.OPERATOR)) return left

            val precedence = operatorPrecedence(opToken.flags[0])
            if (precedence == -1) return left

            if (precedence < minPrecedence) return left

            skip() // operator token
            if (opToken.type == Type.IS) {
                val signature = readSignature(next())
                left = IsStatement(left, signature)
            } else {
                val right =
                    if (opToken.hasFlag(Flag.PRESERVE_ORDER)) element()
                    else parseExpr(precedence)
                left = BinaryOperation(
                    opToken,
                    left,
                    right,
                    opToken.type
                )
            }
        }
        return left
    }

    private fun element(): Expression {
        var left = parseTerm()
        // checks for calling methods located in different classes and also
        //  for array access parsing
        while (notEOF()) {
            val nextOp = peek()
            if (nextOp.type != Type.DOT // (left is class) trying to call a method on an object. e.g. person.sayHello()
                && !(nextOp.type == Type.OPEN_CURVE && !isLiteral(left)) // (left points/is a unit)
                && nextOp.type != Type.OPEN_SQUARE // array element access
                && nextOp.type != Type.DOUBLE_COLON // value casting
                && (nextOp.type != Type.COLON) // event registration
            ) break
            if (nextOp.type == Type.COLON && !left.sig().isJava()) break

            left = when (nextOp.type) {
                // calling shadow func
                Type.OPEN_CURVE -> unitCall(left)
                Type.OPEN_SQUARE -> {
                    // array access
                    skip()
                    val expr = statement()
                    eat(Type.CLOSE_SQUARE)
                    ArrayAccess(nextOp, left, expr)
                }
                Type.DOUBLE_COLON -> {
                    index++
                    Cast(nextOp, left, readSignature(next()))
                }
                else -> javaCall(left)
            }
        }
        return left
    }

    private fun isLiteral(expression: Expression) = when (expression) {
        is IntLiteral,
        is StringLiteral,
            -> true

        is BoolLiteral -> true
        is CharLiteral -> true
        is ArrayLiteral -> true
        else -> false
    }

    private fun javaCall(left: Expression): Expression {
        index++ // a dot
        val where = eat(Type.ALPHA)
        val name = where.data as String

        val clazz = left.sig().javaClass(where)
        if (!consume(Type.OPEN_CURVE)) {
            // Oh! It's field access
            val field = clazz.fields.find { it.name == name }
            if (field == null) {
                where.error<String>("Cannot find field '$name' in class $clazz")
                throw RuntimeException()
            }
            return JavaField(
                where,
                left,
                field,
                Signature.signFromJavaClass(field.type)
            )
        }
        // a method call!
        val args = args()
        eat(Type.CLOSE_CURVE)

        val method = Mirror.lookupMethod(name, clazz, args.map { it.sig().javaClass() })
        return JavaMethodCall(
            where,
            left,
            method,
            args,
            Signature.signFromJavaClass(method.returnType)
        )
    }

    private fun newStatement(token: Token, arguments: List<Expression>): NewInstance {
        val clazz = (token.data as String).let { environment.classes[it] ?: token.error("Cannot find symbol '$it'") }
        val constructor = Mirror.lookupConstructor(clazz, arguments.map { it.sig().javaClass() })
        return NewInstance(
            clazz,
            clazz.name,
            constructor,
            arguments
        )
    }

    private fun operatorPrecedence(type: Flag) = when (type) {
        Flag.ASSIGNMENT_TYPE -> 1
        Flag.IS -> 2
        Flag.LOGICAL_OR -> 3
        Flag.LOGICAL_AND -> 4
        Flag.BITWISE_OR -> 5
        Flag.BITWISE_AND -> 6
        Flag.EQUALITY -> 7
        Flag.RELATIONAL -> 8
        Flag.BINARY -> 9
        Flag.BINARY_L2 -> 10
        Flag.BINARY_L3 -> 11
        else -> -1
    }

    private fun parseTerm(): Expression {
        val token = next()
        val type = token.type
        when {
            type == Type.OPEN_CURVE -> {
                val expr = statement()
                eat(Type.CLOSE_CURVE)
                return expr
            }

            type == Type.MAKE_LIST -> return makeList(token)
            type == Type.MAKE_DICT -> return makeDict(token)
            token.hasFlag(Flag.VALUE) -> return parseValue(token)
            // TODO:
            //  Note: it previously used to call parseTerm() but we changed to parseElement()
            /// Just remember this if something goes wrong while parsing the syntax!
            token.hasFlag(Flag.UNARY) -> return UnaryOperation(token, token.type, element(), true)

            type == Type.ARRAY_OF -> {
                if (isNext(Type.OPEN_CURVE)) {
                    skip()
                    return arrayStatement(token)
                } else {
                    // not for array allocation, array declaration with initial elements
                    eat(Type.LEFT_DIAMOND)
                    val elementSignature = readSignature(next())
                    eat(Type.RIGHT_DIAMOND)
                    eat(Type.OPEN_CURVE)
                    return arrayStatementSignature(token, elementSignature)
                }
            }

            type == Type.MAKE_ARRAY -> {
                eat(Type.LEFT_DIAMOND)
                val elementSignature = readSignature(next())
                eat(Type.RIGHT_DIAMOND)

                eat(Type.OPEN_CURVE)
                val size = statement()
                eat(Type.COMMA)
                val defaultValue = statement()
                eat(Type.CLOSE_CURVE)

                return ArrayAllocation(token, elementSignature, size, defaultValue)
            }
        }
        index--
        if (canParseNext()) return statement()
        return token.error("Unexpected token")
    }


    private fun makeList(where: Token): MakeList {
        val elements = between(Type.OPEN_CURVE, Type.CLOSE_CURVE) { args() }
        return MakeList(where, elements)
    }

    private fun makeDict(where: Token): MakeDictionary {
        val elements = ArrayList<Pair<Expression, Expression>>()
        eat(Type.OPEN_CURVE)
        while (notEOF() && !isNext(Type.CLOSE_CURVE)) {
            val key = statement()
            eat(Type.COLON)
            val value = statement()
            elements += key to value
            if (!consume(Type.COMMA)) break
        }
        eat(Type.CLOSE_CURVE)
        return MakeDictionary(where, elements)
    }

    private fun arrayStatement(token: Token): ArrayLiteral {
        // auto array where signature is decided based on elements
        val arrayElements = parseArrayElements()
        return ArrayLiteral(token, arrayElements)
    }

    private fun arrayStatementSignature(token: Token, signature: Signature): ExplicitArrayLiteral {
        // there's an explicit set signature for the array
        val arrayElements = parseArrayElements()
        return ExplicitArrayLiteral(token, signature, arrayElements)
    }

    private fun parseArrayElements(): MutableList<Expression> {
        val arrayElements = mutableListOf<Expression>()
        if (peek().type != Type.CLOSE_CURVE) {
            while (true) {
                arrayElements.add(statement())
                val next = next()
                val nextType = next.type

                if (nextType == Type.CLOSE_CURVE) break
                else if (nextType != Type.COMMA) next.error<String>("Expected comma for array element separator")
            }
        }
        return arrayElements
    }

    private fun parseValue(token: Token) = when (token.type) {
        Type.NIL -> NilLiteral()
        Type.E_TRUE, Type.E_FALSE -> BoolLiteral(token, token.type == Type.E_TRUE)
        Type.E_INT -> IntLiteral(token, token.data.toString().toInt())
        Type.E_FLOAT -> FloatLiteral(token, token.data.toString().toFloat())
        Type.E_DOUBLE -> DoubleLiteral(token, token.data.toString().toDouble())
        Type.E_STRING -> StringLiteral(token, token.data as String)
        Type.E_CHAR -> CharLiteral(token, token.data as Char)
        Type.ALPHA -> {
            val name = readAlpha(token)
            val vrReference = manager.resolveVr(name)
            if (vrReference == null) {
                if (manager.hasFunctionNamed(name))
                    Alpha(token, -3, name, SignatureConstants.NONE)
                else {
                    val javaClass = environment.classes[name]
                    if (javaClass != null) Alpha(token, -4, name, ClassSignature(javaClass))
                    else token.error("Cannot find symbol '$name'")
                }
            } else {
                // classic variable access
                Alpha(token, vrReference.index, name, vrReference.signature)
            }
        }
        else -> token.error("Unknown token type")
    }

    private fun unitCall(alphaExpr: Expression): Expression {
        if (alphaExpr !is Alpha) {
            val message =
                "Expected a function name for method call, bug got type ${alphaExpr.sig()}"
            // fallback message
            throw RuntimeException(message)
        }
        eat(Type.OPEN_CURVE)
        val arguments = args()
        eat(Type.CLOSE_CURVE)
        val fnExpr = manager.resolveFn(alphaExpr.value, arguments.size)
        if (fnExpr != null) {
            if (fnExpr.argsSize == -1)
                throw RuntimeException("[Internal] Function args size is not yet set")
            return MethodCall(alphaExpr.where, fnExpr, arguments)
        }
        return newStatement(alphaExpr.where, arguments)
    }

    private fun callArguments(): List<Expression> {
        eat(Type.OPEN_CURVE)
        val arguments = args()
        eat(Type.CLOSE_CURVE)
        return arguments
    }

    private fun args(): List<Expression> {
        while (notEOF() && isNext(Type.CLOSE_CURVE)) return emptyList()
        val expressions = ArrayList<Expression>()
        while (notEOF()) {
            expressions += statement()
            if (isNext(Type.COMMA)) index++ else break
        }
        return expressions
    }

    private fun readAlpha(): String {
        val token = next()
        if (token.type != Type.ALPHA) return token.error("Expected alpha token got $token")
        return token.data as String
    }

    private fun readAlpha(token: Token) =
        if (token.type == Type.ALPHA) token.data as String
        else token.error("Was expecting an alpha token")


    private fun eat(type: Type) = next().let {
        if (it.type != type) it.error("Expected token type $type but got ${it.type}")
        else it
    }

    private fun consume(type: Type): Boolean {
        if (isNext(type)) {
            index++
            return true
        }
        return false
    }

    private fun isNext(type: Type) = !isEOF() && peek().type == type

    private fun back() {
        index--
    }

    private fun skip() {
        index++
    }

    private fun <T> between(start: Type, end: Type, block: () -> T): T {
        eat(start)
        val t = block()
        eat(end)
        return t
    }

    private fun next(): Token {
        if (isEOF()) throw RuntimeException("Early EOF")
        return tokens[index++]
    }

    private fun peek(): Token {
        if (isEOF()) throw RuntimeException("Early EOF")
        return tokens[index]
    }

    private fun notEOF() = index < size
    private fun isEOF() = index == size

    private fun List<Expression>.toSignString(): String {
        val string = StringJoiner(", ")
        for (expression in this) string.add(expression.sig().logName())
        return string.toString()
    }

}