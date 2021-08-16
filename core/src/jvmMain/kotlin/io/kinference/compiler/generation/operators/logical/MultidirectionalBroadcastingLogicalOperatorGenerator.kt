package io.kinference.compiler.generation.operators.logical

import io.kinference.compiler.generation.operators.common.MultidirectionalBroadcastingOperatorGenerator
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.utils.DataTypeInfo
import io.kinference.compiler.generation.utils.exact
import io.kinference.operators.Operator
import io.kinference.primitives.types.DataType
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
abstract class MultidirectionalBroadcastingLogicalOperatorGenerator(
    operator: Operator<*, *>,
    info: OperatorGenerationInfo
) : MultidirectionalBroadcastingOperatorGenerator(operator, info) {
    override val resultType: DataTypeInfo
        get() = DataType.BOOLEAN.exact()
}
