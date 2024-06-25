package space.themelon.eia64.runtime

import space.themelon.eia64.syntax.Type

class ArrayElement(
    private val array: Any,
    private val index: Int,
    value: Any,
    type: Type,
): Entity("ArrayElement", true, value, type) {
    override fun update(another: Any) {
        if (type != Type.E_ANY) {
            val updateType = getType(another)
            if (type != updateType)
                throw RuntimeException("ArrayElement cannot update an object of type $type to $updateType")
        }
        // TODO:
        //  in future we need to create a wrapping against primitive types to be able to directly update values
        if (array is String) {
            array[index]
        }
    }
}