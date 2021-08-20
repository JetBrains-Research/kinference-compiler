package io.kinference.compiler.generation.operators.flow

import com.squareup.kotlinpoet.CodeBlock
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.operators.math.MultidirectionalBroadcastingArithmeticOperatorGenerator
import io.kinference.compiler.generation.utils.DataTypeInfo
import io.kinference.operators.flow.Where
import kotlin.time.ExperimentalTime

/**
 * Where generator.
 *
 * [ONNX documentation](https://github.com/onnx/onnx/blob/master/docs/Operators.md#Where)
 *
 * KInference class: [Where]
 */
@OptIn(ExperimentalTime::class)
class WhereGenerator(
    operator: Where,
    info: OperatorGenerationInfo
) : MultidirectionalBroadcastingArithmeticOperatorGenerator(operator, info) {
    override val resultType: DataTypeInfo
        get() = inputInfo.last().dataType

    override fun operatorImpl(inputs: List<String>, output: String, resultType: DataTypeInfo): CodeBlock =
        CodeBlock.of("$output = if (${inputs[0]}) ${inputs[1]} else ${inputs[2]}")
}
