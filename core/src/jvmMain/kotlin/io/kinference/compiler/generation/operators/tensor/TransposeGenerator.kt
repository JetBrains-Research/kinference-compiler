package io.kinference.compiler.generation.operators.tensor

import io.kinference.compiler.generation.info.TensorInfo
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.operators.common.TypeAndShapeAwareOperatorGenerator
import io.kinference.ndarray.reversed
import io.kinference.ndarray.toIntArray
import io.kinference.operators.tensor.Transpose
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class TransposeGenerator(
    private val operator: Transpose,
    info: OperatorGenerationInfo
) : TypeAndShapeAwareOperatorGenerator(operator, info) {

    private val perm = operator.getAttributeOrNull<LongArray>("perm")?.toIntArray()

    private fun actualPerm(shape: IntArray) = perm ?: shape.indices.reversed()

    override fun resultInfo(): Map<String, TensorInfo> = mapOf(
        operator.outputs.first() to TensorInfo(
            shape = inputInfo.first().shape.let { shape -> actualPerm(shape).map { shape[it] }.toIntArray() },
            dataType = inputInfo.first().dataType
        )
    )
}
