package io.kinference.compiler.generation.operators.logical

import com.squareup.kotlinpoet.CodeBlock
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.utils.DataTypeInfo
import io.kinference.operators.logical.Equal
import kotlin.time.ExperimentalTime

/**
 * Equal generator.
 *
 * [ONNX documentation](https://github.com/onnx/onnx/blob/master/docs/Operators.md#Equal)
 *
 * KInference class: [Equal]
 */
@OptIn(ExperimentalTime::class)
class EqualGenerator(
    operator: Equal,
    info: OperatorGenerationInfo
) : MultidirectionalBroadcastingLogicalOperatorGenerator(operator, info) {
    override fun operatorImpl(inputs: List<String>, output: String, resultType: DataTypeInfo): CodeBlock =
        CodeBlock.of("$output = ${inputs[0]} == ${inputs[1]}")
}
