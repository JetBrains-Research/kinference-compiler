package io.kinference.compiler.generation.operators.math

import io.kinference.compiler.generation.operators.common.MultidirectionalBroadcastingOperatorGenerator
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.utils.DataTypeInfo
import io.kinference.operators.Operator
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
abstract class MultidirectionalBroadcastingArithmeticOperatorGenerator(
    operator: Operator<*, *>,
    info: OperatorGenerationInfo
) : MultidirectionalBroadcastingOperatorGenerator(operator, info) {
    override fun resultType(inputType: DataTypeInfo): DataTypeInfo = inputType
}
