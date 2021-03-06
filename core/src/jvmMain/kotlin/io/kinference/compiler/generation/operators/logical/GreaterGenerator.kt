package io.kinference.compiler.generation.operators.logical

import com.squareup.kotlinpoet.CodeBlock
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.utils.DataTypeInfo
import io.kinference.operators.logical.Greater
import kotlin.time.ExperimentalTime

/**
 * Greater generator.
 *
 * [ONNX documentation](https://github.com/onnx/onnx/blob/master/docs/Operators.md#Greater)
 *
 * KInference class: [Greater]
 */
@OptIn(ExperimentalTime::class)
class GreaterGenerator(
    operator: Greater,
    info: OperatorGenerationInfo
) : MultidirectionalBroadcastingLogicalOperatorGenerator(operator, info) {
    override fun operatorImpl(inputs: List<String>, output: String, resultType: DataTypeInfo): CodeBlock =
        CodeBlock.of("$output = ${inputs[0]} > ${inputs[1]}")
}
