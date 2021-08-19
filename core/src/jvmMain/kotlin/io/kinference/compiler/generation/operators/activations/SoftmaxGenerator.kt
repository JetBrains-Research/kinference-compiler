package io.kinference.compiler.generation.operators.activations

import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.operators.activations.Softmax
import kotlin.time.ExperimentalTime

/**
 * Softmax generator.
 *
 * [ONNX documentation](https://github.com/onnx/onnx/blob/master/docs/Operators.md#Softmax)
 *
 * KInference class: [Softmax]
 */
@OptIn(ExperimentalTime::class)
class SoftmaxGenerator(
    private val operator: Softmax,
    info: OperatorGenerationInfo
) : ActivationGenerator(operator, info)
