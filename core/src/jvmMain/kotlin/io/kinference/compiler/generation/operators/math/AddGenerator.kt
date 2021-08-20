package io.kinference.compiler.generation.operators.math

import com.squareup.kotlinpoet.CodeBlock
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.utils.DataTypeInfo
import io.kinference.compiler.generation.utils.dataTypeName
import io.kinference.operators.math.Add
import kotlin.time.ExperimentalTime

/**
 * Add generator.
 *
 * [ONNX documentation](https://github.com/onnx/onnx/blob/master/docs/Operators.md#Add)
 *
 * KInference class: [Add]
 */
@OptIn(ExperimentalTime::class)
class AddGenerator(
    operator: Add,
    info: OperatorGenerationInfo
) : MultidirectionalBroadcastingArithmeticOperatorGenerator(operator, info) {
    override fun operatorImpl(inputs: List<String>, output: String, resultType: DataTypeInfo): CodeBlock =
        CodeBlock.of("$output = (${inputs[0]} + ${inputs[1]}).to${resultType.dataTypeName()}()")
}
