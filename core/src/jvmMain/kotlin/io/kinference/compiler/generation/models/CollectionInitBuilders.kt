package io.kinference.compiler.generation.models

import com.squareup.kotlinpoet.CodeBlock
import io.kinference.compiler.generation.utils.addLine

/* Base class for collection initialization (e. g. listOf, mapOf) */
abstract class CollectionInitBuilder: CodeBlockGenerator() {
    var size: Int = 0
        private set

    fun addItem(item: CodeBlock) {
        builder.apply {
            add(item)
            addLine(",")
        }
        size++
    }

    override fun generateImpl() {
        builder.apply {
            unindent()
            add(")")
        }
    }
}

class ListInitBuilder: CollectionInitBuilder() {
    init {
        builder.apply {
            addLine("listOf(")
            indent()
        }
    }
}

class MapInitBuilder: CollectionInitBuilder() {
    init {
        builder.apply {
            addLine("mapOf(")
            indent()
        }
    }
}
