package io.kinference.compiler.generation.utils

class IndexStorage {
    private val indexToBounds: MutableMap<String, Pair<Any, Any>> = HashMap()

    fun put(index: String, bounds: Pair<Any, Any>) {
        indexToBounds[index] = bounds
    }

    fun bounds(index: String): Pair<Any, Any> = indexToBounds.getValue(index)

    fun inline(index: String): String = index.inlineIndex(indexToBounds.getValue(index))

    fun inlineLiteral(index: String): Literal = inline(index).toLiteral()
}
