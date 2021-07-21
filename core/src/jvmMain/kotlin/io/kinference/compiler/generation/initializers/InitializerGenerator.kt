package io.kinference.compiler.generation.initializers

import com.squareup.kotlinpoet.CodeBlock
import io.kinference.compiler.generation.models.CodeBlockGenerator
import io.kinference.data.ONNXData
import io.kinference.data.tensors.Tensor
import java.io.File

class InitializerGenerator(
    private val outputDirectory: File,
    private val resourcePath: String,
    private val data: ONNXData,
    private val nameMapping: (String) -> String
) : CodeBlockGenerator() {
    override fun generateImpl() {}

    override fun generate(): CodeBlock =
        when (data) {
            is Tensor -> TensorInitializerGenerator(outputDirectory, resourcePath, data, nameMapping).generate()
            else -> error("ONNXDataType '${data.type.name}' not supported")
        }
}
