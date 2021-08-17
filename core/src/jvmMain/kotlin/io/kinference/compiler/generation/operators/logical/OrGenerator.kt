package io.kinference.compiler.generation.operators.logical

import com.squareup.kotlinpoet.CodeBlock
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.utils.DataTypeInfo
import io.kinference.operators.logical.Or
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class OrGenerator(
    operator: Or,
    info: OperatorGenerationInfo
) : MultidirectionalBroadcastingLogicalOperatorGenerator(operator, info) {
    override fun operatorImpl(inputs: List<String>, output: String, resultType: DataTypeInfo): CodeBlock =
        CodeBlock.of("$output = ${inputs[0]} || ${inputs[1]}")
}
