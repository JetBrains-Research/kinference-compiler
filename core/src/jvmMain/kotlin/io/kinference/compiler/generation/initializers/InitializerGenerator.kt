package io.kinference.compiler.generation.initializers

import com.squareup.kotlinpoet.CodeBlock
import io.kinference.compiler.generation.models.CodeBlockGenerator
import io.kinference.data.ONNXData
import io.kinference.data.tensors.Tensor
import java.io.File

/* Saves graph initializers into resources and generates code for extraction. */
class InitializerGenerator(
    private val outputDirectory: File,
    private val resourcePath: String,
    private val data: ONNXData
) : CodeBlockGenerator() {
    override fun generateImpl() {}

    override fun generate(): CodeBlock =
        when (data) {
            is Tensor -> TensorInitializerGenerator(outputDirectory, resourcePath, data).generate()
            else -> error("ONNXDataType '${data.type.name}' not supported")
        }
}
