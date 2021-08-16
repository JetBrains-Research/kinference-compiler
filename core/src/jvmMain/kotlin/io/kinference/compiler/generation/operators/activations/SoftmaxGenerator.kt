package io.kinference.compiler.generation.operators.activations

import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.operators.activations.Softmax
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class SoftmaxGenerator(
    private val operator: Softmax,
    info: OperatorGenerationInfo
) : ActivationGenerator(operator, info)
