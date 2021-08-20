package io.kinference.compiler.generation.operators.activations

import io.kinference.compiler.generation.info.TensorInfo
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.operators.common.TypeAndShapeAwareOperatorGenerator
import io.kinference.operators.activations.Activation
import kotlin.time.ExperimentalTime

/* Base class for activation operator generators. */
@OptIn(ExperimentalTime::class)
abstract class ActivationGenerator(
    private val operator: Activation,
    info: OperatorGenerationInfo
) : TypeAndShapeAwareOperatorGenerator(operator, info) {
    override fun resultInfo(): Map<String, TensorInfo> = mapOf(
        operator.outputs.first() to inputInfo.first()
    )
}
