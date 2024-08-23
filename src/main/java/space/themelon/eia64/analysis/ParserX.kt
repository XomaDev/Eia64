package space.themelon.eia64.analysis

import space.themelon.eia64.Expression
import space.themelon.eia64.expressions.*
import space.themelon.eia64.expressions.ArrayLiteral
import space.themelon.eia64.runtime.Executor
import space.themelon.eia64.signatures.*
import space.themelon.eia64.signatures.Matching.matches
import space.themelon.eia64.syntax.Flag
import space.themelon.eia64.syntax.Token
import space.themelon.eia64.syntax.Type
import java.io.File
import java.util.StringJoiner

class ParserX(
    private val executor: Executor,
) {

    private val manager = ScopeManager()

    private lateinit var tokens: List<Token>
    private var index = 0
    private var size = 0

    lateinit var parsed: ExpressionList

    fun parse(tokens: List<Token>): ExpressionList {
        println("Parsing $tokens")
        index = 0
        size = tokens.size
        this.tokens = tokens

        val expressions = ArrayList<Expression>()
        println("done")
        parseScopeOutline()
        println("done!")
        while (!isEOF()) expressions.add(parseStatement())
        println("done2")
        println(expressions)
        parsed = ExpressionList(expressions)
        parsed.sig() // necessary
        return parsed
    }

    // This shall not be used for now, since it interferes with live mode and
    // easy access; when language is very mature, then we shall enable it
    private fun parseClass(): Expression {
        val token = next()
        if (token.flags.isNotEmpty()) {
            if (token.flags[0] == Flag.V_KEYWORD) return variableDeclaration(false, token)
            else if (token.flags[0] == Flag.MODIFIER) return handleModifier(token)
        }
        if (token.type == Type.FUN) return fnDeclaration()
        else if (token.type == Type.INCLUDE) return includeStatement()
        return token.error("Unexpected token")
    }


    // make sure to update canParseNext() when we add stuff here!
    private fun parseStatement(): Expression {
        val token = next()
        if (token.flags.isNotEmpty()) {
            if (token.flags[0] == Flag.LOOP) return loop(token)
            else if (token.flags[0] == Flag.V_KEYWORD) return variableDeclaration(false, token)
            else if (token.flags[0] == Flag.INTERRUPTION) return interruption(token)
            else if (token.flags[0] == Flag.MODIFIER) return handleModifier(token)
            else {
            }
        }
        return if (token.type == Type.IF) ifDeclaration(token)
        else if (token.type == Type.FUN) fnDeclaration()
        else if (token.type == Type.SHADO) shadoDeclaration()
        else if (token.type == Type.NEW) newStatement(token)
        else if (token.type == Type.INCLUDE) includeStatement()
        else if (token.type == Type.WHEN) whenStatement(token)
        else if (token.type == Type.THROW) throwStatement(token)
        else if (token.type == Type.TRY) tryCatchStatement(token)
        else {
            back()
            parseExpr(0)
        }
    }

    private fun canParseNext(): Boolean {
        val token = peek()
        if (token.flags.isNotEmpty())
            token.flags[0].let {
                if (it == Flag.LOOP
                    || it == Flag.V_KEYWORD
                    || it == Flag.INTERRUPTION
                    || it == Flag.MODIFIER
                )
                    return true
            }
        val type = token.type;
        return type == Type.IF
                || type == Type.FUN
                || type == Type.STD
                || type == Type.INCLUDE
                || type == Type.NEW
                || type == Type.THROW
                || type == Type.TRY
                || type == Type.SHADO
                || type == Type.WHEN
    }

    private fun parseScopeOutline() {
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
            val type = token.type
            if (type == Type.OPEN_CURLY) curlyBracesCount++
            else if (type == Type.CLOSE_CURLY) {
                if (curlyBracesCount == 0) break
                else curlyBracesCount--
            } else if (type == Type.FUN) {
                if (curlyBracesCount == 0) handleFn(false)
            } else if (type == Type.VISIBLE || type == Type.INVISIBLE) {
                if (curlyBracesCount == 0 && isNext(Type.FUN)) {
                    skip()
                    handleFn(type == Type.VISIBLE)
                }
            } else {
                println("else: $type")
            }
        }
        index = originalIndex
    }

    private fun handleModifier(modifier: Token): Expression {
        val visible = modifier.type == Type.VISIBLE
        val next = next()
        return if (next.type == Type.FUN) fnDeclaration()
        else if (next.type == Type.VAR || next.type == Type.LET) variableDeclaration(visible, next)
        else next.error("Unexpected token")
    }

    private fun throwStatement(token: Token) = ThrowExpr(token, parseStatement())

    private fun tryCatchStatement(token: Token): TryCatch {
        // try { ... } catch message { ... }
        val tryBlock = autoBodyExpr()
        expectType(Type.CATCH)
        val catchIdentifier = readAlpha()
        manager.enterScope()
        manager.defineVariable(catchIdentifier, false, Sign.STRING, false)
        val catchBody = unscoppedBodyExpr()
        manager.leaveScope()

        return TryCatch(token, tryBlock, catchIdentifier, catchBody)
    }

    private fun includeStatement(): Expression {
        expectType(Type.OPEN_CURVE)
        val classNames = mutableListOf<String>()
        while (true) {
            val next = next()
            if (next.type
                // means importing only one static instance of the class
                == Type.STATIC
            ) {
                expectType(Type.COLON)
                val includeType = next()
                if (includeType.type != Type.STD) {
                    return includeType.error("Eia Web only supports including Std Libs")
                }
                val moduleName = readAlpha()
                val fileUrl = "${Executor.STD_LIB}/${moduleName}.eia"
                manager.classes.add(moduleName)
                manager.staticClasses.add(moduleName)
                executor.addModule(fileUrl, moduleName)
                classNames.add(moduleName)
            }
            else if (next.type == Type.STD) {
                expectType(Type.COLON)
                if (isNext(Type.E_STRING)) {
                    next().error<String>("Eia Web only supports including Std Libs")
                }
                val moduleName = readAlpha()
                val fileUrl = "${Executor.STD_LIB}/$moduleName.eia"
                classNames.add(moduleName)

                manager.classes.add(moduleName)
                executor.addModule(fileUrl, moduleName)
            }
            else if (next.type == Type.E_STRING) throw RuntimeException("Eia Web only supports including Std Libs")
            else next.error("Unexpected token")
            if (peek().type == Type.CLOSE_CURVE) break
            expectType(Type.COMMA)
        }
        expectType(Type.CLOSE_CURVE)
        return Include(classNames)
    }

    private fun getModulePath(path: String) =
        if (path.startsWith('/')) File(path)
        else File("NONE/$path")

    private fun newStatement(token: Token): NewObj {
        val module = readAlpha()
        val arguments = callArguments()
        val argsSize = arguments.size

        val reference = executor.getModule(module).resolveGlobalFn(token, "init", argsSize)
        if (reference == null) {
            token.error<String>("Could not find init(${arguments.toSignString()}) function")
            throw RuntimeException() // never reached
        }
        return NewObj(
            token,
            module,
            arguments,
            reference
        )
    }

    private fun parseNextInBrace(): Expression {
        // we do it this way, just calling parseNext() would work, but it increases code flow
        // which may make it harder to debug the Parser.

        expectType(Type.OPEN_CURVE)
        val expr = parseStatement()
        expectType(Type.CLOSE_CURVE)
        return expr
    }

    private fun whenStatement(where: Token): Expression {
        val expr = parseNextInBrace()

        // Scope: Automatic
        fun readStatement(): Scope {
            expectType(Type.RIGHT_ARROW)
            return autoScopeBody()
        }

        expectType(Type.OPEN_CURLY)
        val matches = ArrayList<Pair<Expression, Scope>>()
        while (true) {
            val p = peek()
            if (p.type == Type.ELSE) break
            if (p.type == Type.CLOSE_CURLY) {
                where.error<String>("Expected else branch for the when statement")
            }
            matches.add(parseStatement() to readStatement())
        }
        expectType(Type.ELSE)
        val elseBranch = readStatement()
        expectType(Type.CLOSE_CURLY)

        if (matches.isEmpty()) {
            where.error<String>("When statement cannot be empty")
        }
        return When(where, expr, matches, elseBranch)
    }

    private fun loop(where: Token): Expression {
        if (where.type == Type.UNTIL) {
            val expr = parseNextInBrace()
            // Scope: Automatic
            val body = manager.iterativeScope { autoScopeBody() }
            return Until(where, expr, body)
        }
        else if (where.type == Type.FOR) return forLoop(where)
        else if (where.type == Type.EACH) {
            expectType(Type.OPEN_CURVE)
            val iName = readAlpha()
            expectType(Type.COLON)

            val from = parseStatement()
            expectType(Type.TO)
            val to = parseStatement()

            var by: Expression? = null
            if (isNext(Type.BY)) {
                skip()
                by = parseStatement()
            }
            expectType(Type.CLOSE_CURVE)
            manager.enterScope()
            manager.defineVariable(iName, false, Sign.INT, false)
            // Manual Scopped!
            val body = manager.iterativeScope { unscoppedBodyExpr() }
            manager.leaveScope()
            return Itr(where, iName, from, to, by, body)
        }
        else return where.error("Unknown loop type symbol")
    }

    private fun forLoop(where: Token): Expression {
        // we cannot expose initializers outside the for loop
        expectType(Type.OPEN_CURVE)
        val expression = if (isNext(Type.ALPHA)) forEach(where) else forVariableLoop(where)
        return expression
    }

    private fun forEach(where: Token): ForEach {
        val iName = readAlpha()
        expectType(Type.IN)
        val entity = parseStatement()
        expectType(Type.CLOSE_CURVE)

        val iterableSignature = entity.sig()
        val elementSignature = if (iterableSignature is ArrayExtension) {
            iterableSignature.elementSignature
        } else if (iterableSignature == Sign.ARRAY) Sign.ANY
        else if (iterableSignature == Sign.STRING) Sign.CHAR
        else {
            where.error<String>("Unknown non iterable element for '$iName'")
            throw RuntimeException()
        }

        manager.enterScope()
        manager.defineVariable(iName, false, elementSignature, false)
        // Manual Scopped!
        val body = manager.iterativeScope { unscoppedBodyExpr() }
        manager.leaveScope()
        return ForEach(where, iName, entity, body)
    }

    private fun forVariableLoop(
        where: Token,
    ): ForLoop {
        manager.enterScope()
        val initializer = if (isNext(Type.SEMI_COLON)) null else parseStatement()
        expectType(Type.SEMI_COLON)
        val conditional = if (isNext(Type.SEMI_COLON)) null else parseStatement()
        expectType(Type.SEMI_COLON)
        val operational = if (isNext(Type.CLOSE_CURVE)) null else parseStatement()
        expectType(Type.CLOSE_CURVE)
        // double layer scope wrapping
        // Scope: Automatic
        val body = manager.iterativeScope { autoBodyExpr() }
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
            if (token.type == Type.RETURN) {
                val expectedSignature = manager.getPromisedSignature
                if (expectedSignature == Sign.NONE) {
                    null
                } else {
                    val expr = parseStatement()
                    val gotSignature = expr.sig()
                    if (!matches(expectedSignature, gotSignature)) {
                        token.error<String>("Was expecting return type of $expectedSignature but got $gotSignature")
                        throw RuntimeException()
                    }
                    expr
                }
            }
            else if (token.type == Type.USE) parseStatement()
            else null
        )
    }

    private fun functionOutline(public: Boolean): FunctionReference {
        val where = next()
        val name = readAlpha(where)

        expectType(Type.OPEN_CURVE)
        val requiredArgs = mutableListOf<Pair<String, Signature>>()
        while (!isEOF() && peek().type != Type.CLOSE_CURVE) {
            val parameterName = readAlpha()
            expectType(Type.COLON)
            val signature = readSignature(next())

            requiredArgs += parameterName to signature
            if (!isNext(Type.COMMA)) break
            skip()
        }
        expectType(Type.CLOSE_CURVE)

        val isVoid: Boolean
        val returnSignature = if (isNext(Type.COLON)) {
            skip()
            isVoid = false
            readSignature(next())
        } else {
            isVoid = true
            Sign.UNIT
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

    private fun fnDeclaration(): FunctionExpr {
        val reference = manager.readFnOutline()
        index = reference.tokenIndex
        manager.enterScope()
        reference.parameters.forEach { manager.defineVariable(it.first, true, it.second, false) }

        val body: Expression = if (isNext(Type.ASSIGNMENT)) {
            skip()
            parseStatement()
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

    private fun shadoDeclaration(): Shadow {
        val names = ArrayList<String>()

        manager.enterScope()
        expectType(Type.OPEN_CURVE)
        while (!isEOF() && peek().type != Type.CLOSE_CURVE) {
            val name = readAlpha()
            expectType(Type.COLON)

            val argSignature = readSignature(next())
            manager.defineVariable(name, true, argSignature, false)
            names.add(name)
            if (!isNext(Type.COMMA)) break
            skip()
        }
        expectType(Type.CLOSE_CURVE)
        val body = if (isNext(Type.ASSIGNMENT)) {
            skip()
            parseStatement()
        } else expressions() // Fully Manual Scopped
        manager.leaveScope()
        return Shadow(names, body)
    }

    private fun ifDeclaration(where: Token): IfStatement {
        val logicalExpr = parseNextInBrace()
        val ifBody = autoBodyExpr()

        // All is Auto Scopped!
        // Here we need to know if the Is Statement is terminative or not
        //
        // Case 1 => Terminative (meaning end-of execution if the body is executed)
        //   then => then we treat the rest of the body as an else function

        if (isEOF() || peek().type != Type.ELSE)
            return IfStatement(where, logicalExpr, ifBody, NoneExpression.INSTANCE)
        skip()

        val elseBranch = if (peek().type == Type.IF) ifDeclaration(next())
        else autoBodyExpr()
        return IfStatement(where, logicalExpr, ifBody, elseBranch)
    }

    private fun autoBodyExpr(): Scope {
        // used everywhere where there is no manual scope management is required,
        //  e.g., IfExpr, Until, For
        if (peek().type == Type.OPEN_CURLY) return autoScopeBody()
        manager.enterScope()
        return Scope(parseStatement(), manager.leaveScope())
    }

    private fun autoScopeBody(): Scope {
        manager.enterScope()
        return Scope(expressions(), manager.leaveScope())
    }

    private fun unscoppedBodyExpr(): Expression {
        if (peek().type == Type.OPEN_CURLY) return expressions()
        return parseStatement()
    }

    private fun expressions(): Expression {
        expectType(Type.OPEN_CURLY)
        parseScopeOutline()
        val expressions = ArrayList<Expression>()
        if (peek().type == Type.CLOSE_CURLY)
            return ExpressionList(expressions)
        while (!isEOF() && peek().type != Type.CLOSE_CURLY)
            expressions.add(parseStatement())
        expectType(Type.CLOSE_CURLY)
        // such optimisations may alter expressions behaviour
        // if (expressions.size == 1) return expressions[0]
        return ExpressionList(expressions)
    }

    private fun variableDeclaration(public: Boolean, where: Token): Expression {
        //if (!isNext(Type.EXCLAMATION)) {
        // for now, later when ';' will be swapped with //, we won't need it
        //return readVariableDeclaration(where, public)
        //}
        // '!' mark after let or var represents multi expressions
        //next()
        // note => same modifier applied to all variables
        val expressions = mutableListOf<Expression>()
        do {
            // read minimum one declaration
            expressions += readVariableDeclaration(where, public)
            //println("Iteration: " + expressions.last())
        } while (isNext(Type.COMMA).also { if (it) next() })

        if (expressions.size == 1) return expressions.first()
        return ExpressionBind(expressions)
    }

    private fun readVariableDeclaration(
        where: Token,
        public: Boolean
    ): Expression {
        val name = readAlpha()

        val expr: Expression
        val signature: Signature

        val mutable = where.type == Type.VAR
        if (!isNext(Type.COLON)) {
            val assignmentExpr = readVariableExpr()
            signature = assignmentExpr.sig()
            expr = AutoVariable(where, name, assignmentExpr)
        } else {
            skip()
            signature = readSignature(next())
            expr = ExplicitVariable(
                where,
                where.type == Type.VAR,
                name,
                readVariableExpr(),
                signature
            )
        }
        manager.defineVariable(name, mutable, signature, public)
        return expr
    }

    private fun readSignature(token: Token): Signature {
        if (token.hasFlag(Flag.CLASS)) {
            val classType = token.type
            if (classType == Type.E_NUMBER) {
                return Sign.NUM
            } else if (classType == Type.E_NIL) {
                return Sign.NIL
            } else if (classType == Type.E_INT) {
                return Sign.INT
            } else if (classType == Type.E_FLOAT) {
                return Sign.FLOAT
            } else if (classType == Type.E_STRING) {
                return Sign.STRING
            } else if (classType == Type.E_CHAR) {
                return Sign.CHAR
            } else if (classType == Type.E_BOOL) {
                return Sign.BOOL
            } else if (classType == Type.E_ANY) {
                return Sign.ANY
            } else if (classType == Type.E_UNIT) {
                return Sign.UNIT
            } else if (classType == Type.E_TYPE) {
                return Sign.TYPE
            } else if (classType == Type.E_ARRAY) {
                if (isNext(Type.LEFT_DIAMOND)) {
                    skip()
                    val elementSignature = readSignature(next())
                    expectType(Type.RIGHT_DIAMOND)
                    return ArrayExtension(elementSignature)
                }
                return Sign.ARRAY
            } else if (classType == Type.E_OBJECT) {
                return ObjectExtension(Sign.OBJECT.type) // Generic form
            } else {
                return token.error("Unknown class $classType")
            }
        }

        if (token.type != Type.ALPHA) {
            token.error<String>("Expected a class type")
            // end of execution
        }
        if (manager.classes.contains(token.data as String)) {
            // class that was included from external files
            // this will be an extension of Object class type
            return ObjectExtension(token.data)
        }
        token.error<String>("Unknown class ${token.data}")
        throw RuntimeException()
    }

    private fun readVariableExpr(): Expression {
        val nextToken = peek()
        return if (nextToken.type == Type.ASSIGNMENT) {
            skip()
            parseStatement()
        }
        else if (nextToken.type == Type.OPEN_CURVE) shadoDeclaration()
        else if (nextToken.type == Type.OPEN_CURLY) parseStatement()
        else nextToken.error("Unexpected variable expression")
    }

    private fun parseExpr(minPrecedence: Int): Expression {
        // this parses a full expressions, until it's done!
        var left = parseElement()
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
                // need to verify that variable is mutable
                if (opToken.type == Type.ASSIGNMENT) checkMutability(opToken, left)
                val right =
                    if (opToken.hasFlag(Flag.PRESERVE_ORDER)) parseElement()
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

    private fun parseElement(): Expression {
        var left = parseTerm()
        // checks for calling methods located in different classes and also
        //  for array access parsing
        while (!isEOF()) {
            val nextOp = peek()
            if (nextOp.type != Type.DOT // (left is class) trying to call a method on an object. e.g. person.sayHello()
                && !(nextOp.type == Type.OPEN_CURVE && !isLiteral(left)) // (left points/is a unit)
                && nextOp.type != Type.OPEN_SQUARE // array element access
                && nextOp.type != Type.DOUBLE_COLON // value casting
            ) break

            left = if (nextOp.type
                // calling shadow func
                == Type.OPEN_CURVE
            ) unitCall(left)
            else if (nextOp.type == Type.OPEN_SQUARE) {
                // array access
                val expr = parseStatement()
                expectType(Type.CLOSE_SQUARE)
                ArrayAccess(left.marking!!, left, expr)
            } else if (nextOp.type == Type.DOUBLE_COLON) {
                skip()
                Cast(nextOp, left, readSignature(next()))
            } else classElementCall(left)
        }
        return left
    }

    private fun isLiteral(expression: Expression) = if (expression is IntLiteral || expression is StringLiteral) true
    else if (expression is BoolLiteral) true
    else if (expression is CharLiteral) true
    else if (expression is ArrayLiteral) true
    else false

    private fun checkMutability(where: Token, variableExpression: Expression) {
        // it's fine if it's array access, array elements are always mutable
        if (variableExpression is ArrayAccess) return

        val variableName: String
        val index: Int

        if (variableExpression is Alpha) {
            variableName = variableExpression.value
            index = variableExpression.index
        } else if (variableExpression is ForeignField) {
            variableName = variableExpression.property
            index = variableExpression.uniqueVariable.index
        } else {
            where.error<String>("Expected a variable to assign")
            throw RuntimeException()
        }
        // Variable Index
        // Below 0: represents a name type, a function? a class? etc.
        // Above 0: memory index to know where the variable is stored
        if (index < 0) {
            where.error<String>("Expected a variable to assign")
            throw RuntimeException()
        }
        val mutable = manager.resolveVr(variableName)!!.mutable
        if (!mutable) {
            where.error<String>("Variable $variableName is marked immutable")
            throw RuntimeException()
        }
    }

    private fun classElementCall(objExpr: Expression): Expression {
        skip()

        val element = next()
        val elementName = readAlpha(element)

        val moduleInfo = getModuleInfo(element, objExpr)
        if (isNext(Type.OPEN_CURVE))
            return classMethodCall(objExpr, moduleInfo, elementName)
        return classPropertyAccess(objExpr, moduleInfo, elementName)
    }

    private fun classPropertyAccess(
        objectExpression: Expression,
        moduleInfo: ModuleInfo,
        property: String
    ): ForeignField {
        // Plan:
        //  Global variables of other class are located in scope 0
        //  So we need to just maintain position of that variable in
        //  super scope, then access it at runtime
        val uniqueVariable = executor.getModule(moduleInfo.name).resolveGlobalVr(moduleInfo.where, property)
            ?: moduleInfo.where.error("Could not find global variable '$property' in module ${moduleInfo.name}")
        return ForeignField(
            where = moduleInfo.where,
            static = isStatic(objectExpression),
            objectExpression = objectExpression,
            property = property,
            uniqueVariable,
            moduleInfo,
        )
    }

    private fun classMethodCall(
        objectExpression: Expression,
        moduleInfo: ModuleInfo,
        elementName: String
    ): ClassMethodCall {
        val arguments = callArguments()

        // bump argsSize if it's linked invocation
        val argsSize = if (moduleInfo.linked) arguments.size + 1 else arguments.size

        val fnReference = executor.getModule(moduleInfo.name).resolveGlobalFn(moduleInfo.where, elementName, argsSize)

        if (fnReference == null) {
            moduleInfo.where.error<String>("Could not find $elementName(${arguments.toSignString()}) function in module ${moduleInfo.name}")
            throw RuntimeException() // not reached
        }

        return ClassMethodCall(
            where = objectExpression.marking!!,
            static = isStatic(objectExpression),
            objectExpression = objectExpression,
            method = elementName,
            arguments = arguments,
            reference = fnReference,
            moduleInfo = moduleInfo
        )
    }

    // Whether the expression references an external static
    // included class
    private fun isStatic(expression: Expression) =
        expression is Alpha && manager.staticClasses.contains(expression.value)

    private fun getModuleInfo(
        where: Token,
        objectExpression: Expression
    ): ModuleInfo {
        // First Case: Pure static invocation `Person.sayHello()`
        // Second Case: Linked Static Invocation
        //    let myString = " Meow "
        //    println(myString.trim())
        // Third Case: Object Invocation
        //    let myPerson = new Person("Miaw")
        //    println(myPerson.sayHello())
        if (objectExpression is Alpha && objectExpression.index == -2) {
            // Pure static invocation
            return ModuleInfo(where, objectExpression.value, false)
        } else {
            val signature = objectExpression.sig()
            if (signature is SimpleSignature) {
                // Linked Static Invocation (String, Int, Array)
                return ModuleInfo(where, getLinkedModule(signature, where), true)
            } else if (signature is ObjectExtension) {
                // Object Invocation
                return ModuleInfo(where, signature.extensionClass, false)
            } else {
                // TODO: we'll have to work on a fix for this
                signature as ArrayExtension
                return ModuleInfo(where, "array", true)
            }
        }
    }

    private fun getLinkedModule(
        signature: Signature,
        where: Token
    ) = if (signature == Sign.NONE) where.error("Signature type NONE has no module")
    else if (signature == Sign.ANY) where.error("Signature type ANY has no module")
    else if (signature == Sign.CHAR) where.error("Signature type CHAR has no module")
    else if (signature == Sign.FLOAT) where.error("Signature type FLOAT has no module")
    else if (signature == Sign.UNIT) where.error("Signature type UNIT has no module")
    else if (signature == Sign.OBJECT) where.error("Signature type OBJECT has no module") // (Raw Object sign)
    else if (signature == Sign.INT) "eint"
    else if (signature == Sign.STRING) "string"
    else if (signature == Sign.BOOL) "bool"
    else if (signature == Sign.ARRAY) "array"
    else if (signature == Sign.TYPE) "etype"
    else where.error("Unknown object signature for module link $signature")

    private fun resolveGlobalVr(where: Token, name: String): UniqueVariable? {
        val variable = manager.resolveGlobalVr(name) ?: return null
        if (!variable.public) where.error<String>("Variable $name is marked private")
        return variable
    }

    private fun resolveGlobalFn(where: Token, name: String, numArgs: Int): FunctionReference? {
        val reference = manager.resolveFn(name, numArgs) ?: return null
        if (!reference.public) where.error<String>("Function $name is marked private")
        return reference
    }

    private fun operatorPrecedence(type: Flag) = if (type == Flag.ASSIGNMENT_TYPE) {
        1
    } else if (type == Flag.IS) {
        2
    } else if (type == Flag.LOGICAL_OR) {
        3
    } else if (type == Flag.LOGICAL_AND) {
        4
    } else if (type == Flag.BITWISE_OR) {
        5
    } else if (type == Flag.BITWISE_AND) {
        6
    } else if (type == Flag.EQUALITY) {
        7
    } else if (type == Flag.RELATIONAL) {
        8
    } else if (type == Flag.BINARY) {
        9
    } else if (type == Flag.BINARY_L2) {
        10
    } else if (type == Flag.BINARY_L3) {
        11
    } else {
        -1
    }

    private fun parseTerm(): Expression {
        // a term is only one value, like 'a', '123'
        if (peek().type == Type.OPEN_CURVE) {
            skip()
            val expr = parseStatement()
            expectType(Type.CLOSE_CURVE)
            return expr
        } else if (peek().type == Type.OPEN_CURLY) return Shadow(emptyList(), autoScopeBody().expr)
        val token = next()
        if (token.hasFlag(Flag.VALUE)) {
            val value = parseValue(token)
            if (!token.hasFlag(Flag.CONSTANT_VALUE) // not a hard constant, like `123` or `"Hello, World"`
                && !isEOF()
                && peek().type == Type.OPEN_CURVE
            )
                return unitCall(value)
            return value
        } else if (token.hasFlag(Flag.UNARY)) return UnaryOperation(token, token.type, parseTerm(), true)
        else if (token.hasFlag(Flag.NATIVE_CALL)) {
            val arguments = callArguments()
            return NativeCall(token, token.type, arguments)
        } else if (token.type == Type.ARRAY_OF) {
            if (isNext(Type.OPEN_CURVE)) {
                skip()
                return arrayStatement(token)
            } else {
                // not for array allocation, array declaration with initial elements
                expectType(Type.LEFT_DIAMOND)
                val elementSignature = readSignature(next())
                expectType(Type.RIGHT_DIAMOND)
                expectType(Type.OPEN_CURVE)
                return arrayStatementSignature(token, elementSignature)
            }
        } else if (token.type == Type.MAKE_ARRAY) {
            expectType(Type.LEFT_DIAMOND)
            val elementSignature = readSignature(next())
            expectType(Type.RIGHT_DIAMOND)

            expectType(Type.OPEN_CURVE)
            val size = parseStatement()
            expectType(Type.COMMA)
            val defaultValue = parseStatement()
            expectType(Type.CLOSE_CURVE)

            return ArrayAllocation(token, elementSignature, size, defaultValue)
        }
        back()
        if (canParseNext()) return parseStatement()
        return token.error("Unexpected token")
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
                arrayElements.add(parseStatement())
                val next = next()
                val nextType = next.type

                if (nextType == Type.CLOSE_CURVE) break
                else if (nextType != Type.COMMA) next.error<String>("Expected comma for array element separator")
            }
        }
        return arrayElements
    }

    private fun parseValue(token: Token): Expression {
        return if (token.type == Type.NIL) NilLiteral(token)
        else if (token.type == Type.E_TRUE || token.type == Type.E_FALSE) BoolLiteral(token, token.type == Type.E_TRUE)
        else if (token.type == Type.E_INT) IntLiteral(token, token.data as Int)
        else if (token.type == Type.E_FLOAT) FloatLiteral(token, token.data as Float)
        else if (token.type == Type.E_STRING) StringLiteral(token, token.data as String)
        else if (token.type == Type.E_CHAR) CharLiteral(token, token.data as Char)
        else if (token.type == Type.ALPHA) {
            val name = readAlpha(token)
            val vrReference = manager.resolveVr(name)
            if (vrReference == null) {
                // could be a function call or static invocation
                if (manager.hasFunctionNamed(name))
                // there could be multiple functions with same name
                // but different args, this just marks it as a function
                    Alpha(token, -3, name, Sign.NONE)
                else if (manager.staticClasses.contains(name))
                // probably referencing a method from an outer class
                    Alpha(token, -2, name, Sign.NONE)
                else
                // Unresolved name
                    token.error("Cannot find symbol '$name'")
            } else {
                // classic variable access
                Alpha(token, vrReference.index, name, vrReference.signature)
            }
        } else if (token.type == Type.CLASS_VALUE) parseType(token)
        else if (token.type == Type.OPEN_CURVE) {
            val expr = parseStatement()
            expectType(Type.CLOSE_CURVE)
            expr
        } else token.error("Unknown token type")
    }

    private fun parseType(token: Token): TypeLiteral {
        expectType(Type.DOUBLE_COLON)
        return TypeLiteral(token, readSignature(next()))
    }

    private fun unitCall(unitExpr: Expression): Expression {
        val arguments = callArguments()
        if (unitExpr is Alpha) {
            val name = unitExpr.value
            val fnExpr = manager.resolveFn(name, arguments.size)
            if (fnExpr != null) {
                if (fnExpr.argsSize == -1)
                    throw RuntimeException("[Internal] Function args size is not yet set")
                return MethodCall(unitExpr.marking!!, fnExpr, arguments)
            }
        }
        return ShadoInvoke(unitExpr.marking!!, unitExpr, arguments)
    }

    private fun callArguments(): List<Expression> {
        expectType(Type.OPEN_CURVE)
        val arguments = parseArguments()
        expectType(Type.CLOSE_CURVE)
        return arguments
    }

    private fun parseArguments(): List<Expression> {
        val expressions = ArrayList<Expression>()
        if (!isEOF() && peek().type == Type.CLOSE_CURVE)
            return expressions
        while (!isEOF()) {
            expressions.add(parseStatement())
            if (!isNext(Type.COMMA)) break
            skip()
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

    private fun expectType(type: Type): Token {
        val next = next()
        if (next.type != type)
            next.error<String>("Expected token type $type but got $next")
        return next
    }

    private fun isNext(type: Type) = !isEOF() && peek().type == type

    private fun back() {
        index--
    }

    private fun skip() {
        index++
    }

    private fun next(): Token {
        if (isEOF()) throw RuntimeException("Early EOF")
        return tokens[index++]
    }

    private fun peek(): Token {
        if (isEOF()) throw RuntimeException("Early EOF")
        return tokens[index]
    }

    private fun isEOF() = index == size
}

private fun List<Expression>.toSignString(): String {
    val string = StringBuilder()
    for (expression in this) string.append(expression.sig().logName()).append(" ")
    return string.toString()
}
