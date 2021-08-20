package io.kinference.compiler.generation.context

import io.kinference.compiler.generation.initializers.InitializerGenerator
import io.kinference.compiler.generation.models.CodeBlockGenerator
import io.kinference.compiler.generation.utils.addLine
import io.kinference.data.tensors.Tensor
import io.kinference.graph.Context
import java.io.File

class ContextBuilder(
    private val outputDirectory: File,
    private val resourcePath: String,
): CodeBlockGenerator() {
    init {
        builder.beginControlFlow("%T().apply", Context::class)
    }

    fun addTensor(name: String, value: Tensor) {
        builder.apply {
            add("putValue(%S, ", name)
            add(InitializerGenerator(outputDirectory, resourcePath, value).generate())
            addLine(".asTensor(%S))", name)
        }
    }

    override fun generateImpl() {
        builder.endControlFlow()
    }
}
