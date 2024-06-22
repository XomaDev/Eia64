package space.themelon.eia64.runtime

import java.util.*
import kotlin.collections.ArrayList

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

        fun reset(newSuper: Frame) {
            fSuper = newSuper
            functions.clear()
            values.clear()
        }

        fun applyState(state: Pair<Int, Int>) {
            values.dropLast(values.size - state.first)
            functions.dropLast(functions.size - state.second)
        }
    }

    private var recyclePool: Frame? = null

    private val frameStack = Stack<Frame>()
    private var currentFrame = Frame().also { frameStack.add(it) }

    private fun createFrame() = if (recyclePool == null) {
        Frame(currentFrame)
    } else {
        val tail = recyclePool
        recyclePool = tail?.fSuper

        tail!!.reset(currentFrame)
        tail
    }

    private fun recycle(reusable: Frame) {
        reusable.fSuper = recyclePool
        recyclePool = reusable
    }

    fun enterScope() {
        currentFrame = createFrame()
        frameStack.push(currentFrame)
    }

    fun leaveScope() {
        val reusable = currentFrame
        currentFrame = reusable.fSuper ?: throw RuntimeException("Already reached super scope")

        frameStack.pop()
        recycle(reusable)
    }

    fun declareVar(name: String, value: Any) {
        currentFrame.values.add(Pair(name, value))
    }

    fun declareFn(name: String, value: Any) {
        currentFrame.functions.add(Pair(name, value))
    }

    fun getVar(index: Int, name: String) = currentFrame.searchVr(index, name)

    fun getFn(atFrame: Int, index: Int, name: String): Any {
        val fn = frameStack[atFrame].functions[index]
        if (fn.first != name) throw RuntimeException("Function '$name' does not exist")
        return fn.second
    }

    // returns count of Pair<Variables, Functions>
    fun getStateCount(): Pair<Int, Int> = Pair(currentFrame.values.size, currentFrame.functions.size)
    fun applyStateCount(state: Pair<Int, Int>) {
        currentFrame.applyState(state)
    }

}