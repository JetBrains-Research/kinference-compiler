package io.kinference.compiler.generation.operators.activations

import io.kinference.compiler.generation.info.TensorInfo
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.operators.common.TypeAndShapeAwareOperatorGenerator
import io.kinference.operators.activations.Softmax
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class SoftmaxGenerator(
    private val operator: Softmax,
    info: OperatorGenerationInfo
) : TypeAndShapeAwareOperatorGenerator(operator, info) {
    override fun resultInfo(): Map<String, TensorInfo> = mapOf(
        operator.outputs.first() to inputInfo.first()
    )
}
