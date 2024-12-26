package space.themelon.eia64.analysis

import java.lang.reflect.Constructor
import java.lang.reflect.Method

object Mirror {

    /**
     * We cannot look up for Class<*> at parse time. It must be done at runtime to ensure
     * class is found. Since parsing and evaluation may take place in different environments
     *
     * It also handles cases where Class names may refer to subclasses.
     * MyClass.SubClass (invalid) → MyClass$SubClass (valid)
     */

    fun lookupClass(className: String): Class<*> {
        var lookupName = className
        while (true) {
            try {
                return Class.forName(lookupName)
            } catch (ignored: ClassNotFoundException) {
            }
            lookupName.lastIndexOf('.').let {
                if (it == -1) {
                    throw RuntimeException("Cannot find class $className")
                }
                lookupName = lookupName.substring(0, it) + '$' + lookupName.substring(it + 1)
            }
        }
    }

    fun lookupMethod(name: String, clazz: Class<*>, argTypes: List<Class<*>>): Method {
        return clazz.methods
            .filter {
                it.name == name
                        && it.parameterCount == argTypes.size
                        && it.parameterTypes.withIndex().all { (i, pType) -> pType.isAssignableFrom(argTypes[i]) }
            }
            .minByOrNull {
                it.parameterTypes.withIndex().sumOf { (i, pType) -> computeHierarchyDepth(argTypes[i], pType) }
            }
            ?: throw NoSuchMethodException("Cannot find method $name with arguments $argTypes in $clazz")
    }

    fun lookupConstructor(clazz: Class<*>, argTypes: List<Class<*>>): Constructor<*> {
        return clazz.constructors
            .filter {
                it.parameterCount == argTypes.size
                        && it.parameterTypes.withIndex().all { (i, pType) -> pType.isAssignableFrom(argTypes[i]) }
            }
            .minByOrNull {
                it.parameterTypes.withIndex().sumOf { (i, pType) -> computeHierarchyDepth(argTypes[i], pType) }
            }
            ?: throw NoSuchMethodException("Cannot find constructor with arguments $argTypes in $clazz")
    }

    private fun computeHierarchyDepth(got: Class<*>, expected: Class<*>): Int {
        val argType = unboxType(got)
        val targetType = unboxType(expected)

        var depth = 0
        var currentType: Class<*>? = argType
        while (currentType != null && currentType != targetType) {
            depth++
            currentType = currentType.superclass
        }
        return depth
    }

    private fun unboxType(boxedType: Class<*>) = when (boxedType) {
        Boolean::class.java -> Boolean::class.javaPrimitiveType
        Byte::class.java -> Byte::class.javaPrimitiveType
        Char::class.java -> Char::class.javaPrimitiveType
        Short::class.java -> Short::class.javaPrimitiveType
        Int::class.java -> Int::class.javaPrimitiveType
        Long::class.java -> Long::class.javaPrimitiveType
        Float::class.java -> Float::class.javaPrimitiveType
        Double::class.java -> Double::class.javaPrimitiveType
        Void::class.java -> Void.TYPE
        else -> boxedType
    }
}