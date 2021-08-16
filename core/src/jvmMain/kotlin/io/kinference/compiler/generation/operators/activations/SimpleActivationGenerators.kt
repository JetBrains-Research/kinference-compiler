package io.kinference.compiler.generation.operators.activations

import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.operators.activations.Tanh
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class TanhGenerator(
    private val operator: Tanh,
    info: OperatorGenerationInfo
) : ActivationGenerator(operator, info)