package space.themelon.eia64.mirror

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

    fun lookupMethod(name: String, clazz: Class<*>, lookupTypes: List<Class<*>>): Method {
        val argTypes = lookupTypes.map { unboxType(it) }
        clazz.methods.forEach { println(it) }
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

    fun lookupConstructor(clazz: Class<*>, lookupTypes: List<Class<*>>): Constructor<*> {
        val argTypes = lookupTypes.map { unboxType(it) }
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

    private fun computeHierarchyDepth(argType: Class<*>, expected: Class<*>): Int {
        val targetType = unboxType(expected)

        var depth = 0
        var currentType: Class<*>? = argType
        while (currentType != null && currentType != targetType) {
            depth++
            currentType = currentType.superclass
        }
        return depth
    }

    private fun unboxType(boxedType: Class<*>): Class<*> = when (boxedType) {
        java.lang.Boolean::class.java -> Boolean::class.javaPrimitiveType!!
        java.lang.Byte::class.java -> Byte::class.javaPrimitiveType!!
        java.lang.Character::class.java -> Char::class.javaPrimitiveType!!
        java.lang.Short::class.java -> Short::class.javaPrimitiveType!!
        java.lang.Integer::class.java -> Int::class.javaPrimitiveType!!
        java.lang.Long::class.java -> Long::class.javaPrimitiveType!!
        java.lang.Float::class.java -> Float::class.javaPrimitiveType!!
        java.lang.Double::class.java -> Double::class.javaPrimitiveType!!
        java.lang.Void::class.java -> Void.TYPE
        else -> boxedType
    }
}