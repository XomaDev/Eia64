package space.themelon.eia64.expressions

import space.themelon.eia64.Expression
import space.themelon.eia64.syntax.Token

abstract class PureLiteral(where: Token, open val value: Any?): Expression(where)