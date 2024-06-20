package space.themelon.eia64.runtime

import space.themelon.eia64.Expression

class Memory {

    data class Frame(var fSuper: Frame? = null) {
        var functions = ArrayList<Pair<String, Any>>()
        var values = ArrayList<Pair<String, Any>>()

        fun searchVr(index: Int, name: String): Any {
            if (values.size > index) {
                val get = values[index]
                if (get.first == name) return get.second
            }
            return fSuper?.searchVr(index, name) ?: throw RuntimeException("Unable to find variable '$name'")
        }

        fun searchFn(index: Int, name: String): Any {
            if (functions.size > index) {
                val get = functions[index]
                if (get.first == name) return get.second
            }
            return fSuper?.searchFn(index, name) ?: throw RuntimeException("Unable to find function '$name'")
        }

        fun reset(newSuper: Frame) {
            fSuper = newSuper
            functions.clear()
            values.clear()
        }
    }

    private var pool: Frame? = null
    private var currentFrame = Frame()

    fun enterScope() {
        if (pool == null) {
            currentFrame = Frame(currentFrame)
        } else {
            val tail = pool
            pool = tail?.fSuper

            tail!!.reset(currentFrame)
            currentFrame = tail
        }
    }

    fun leaveScope() {
        val reusable = currentFrame
        currentFrame = reusable.fSuper ?: throw RuntimeException("Already reached super scope")

        reusable.fSuper = pool
        pool = reusable
    }

    fun declareVar(name: String, value: Any) {
        currentFrame.values.add(Pair(name, value))
    }

    fun declareFn(name: String, value: Any) {
        currentFrame.functions.add(Pair(name, value))
    }

    fun getVar(index: Int, name: String) = currentFrame.searchVr(index, name)
    fun getFn(index: Int, name: String) = currentFrame.searchFn(index, name)

}