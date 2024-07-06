package space.themelon.eia64.analysis

import space.themelon.eia64.syntax.Type

enum class ExprType {
    INT,
    BOOL,
    STRING,
    CHAR,
    ARRAY,
    UNIT,
    OBJECT,
    ANY,
    NONE,

    MISMATCHED,
    ;

    companion object {
        fun translate(type: Type) = when (type) {
            Type.E_INT -> INT
            Type.E_BOOL -> BOOL
            Type.E_CHAR -> CHAR
            Type.E_STRING -> STRING
            Type.E_OBJECT -> OBJECT
            Type.E_ARRAY -> ARRAY
            Type.E_ANY -> ANY
            Type.E_UNIT -> UNIT
            else -> throw RuntimeException("Unknown return type translation $type")
        }

        fun translateBack(type: ExprType) = when (type) {
            INT -> Type.E_INT
            BOOL -> Type.E_BOOL
            CHAR -> Type.E_CHAR
            STRING -> Type.E_STRING
            OBJECT -> Type.E_OBJECT
            ARRAY -> Type.E_ARRAY
            ANY -> Type.E_ANY
            UNIT -> Type.E_ANY
            else -> throw RuntimeException("Unknown return type translation $type")
        }

        fun typeEquals(left: ExprType, right: ExprType) = left == right
    }
}