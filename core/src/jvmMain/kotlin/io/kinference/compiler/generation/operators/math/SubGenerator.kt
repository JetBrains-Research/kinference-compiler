package io.kinference.compiler.generation.operators.math

import com.squareup.kotlinpoet.CodeBlock
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.utils.DataTypeInfo
import io.kinference.compiler.generation.utils.dataTypeName
import io.kinference.operators.math.Sub
import kotlin.time.ExperimentalTime

/**
 * Sub generator.
 *
 * [ONNX documentation](https://github.com/onnx/onnx/blob/master/docs/Operators.md#Sub)
 *
 * KInference class: [Sub]
 */
@OptIn(ExperimentalTime::class)
class SubGenerator(
    operator: Sub,
    info: OperatorGenerationInfo
) : MultidirectionalBroadcastingArithmeticOperatorGenerator(operator, info) {
    override fun operatorImpl(inputs: List<String>, output: String, resultType: DataTypeInfo): CodeBlock =
        CodeBlock.of("$output = (${inputs[0]} - ${inputs[1]}).to${resultType.dataTypeName()}()")
}
