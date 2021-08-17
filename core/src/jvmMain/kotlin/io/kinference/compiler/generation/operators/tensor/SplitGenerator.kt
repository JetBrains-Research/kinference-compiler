package io.kinference.compiler.generation.operators.tensor

import io.kinference.compiler.generation.info.TensorInfo
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.operators.common.TypeAndShapeAwareOperatorGenerator
import io.kinference.compiler.generation.utils.actualAxis
import io.kinference.ndarray.toIntArray
import io.kinference.operators.tensor.Split
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class SplitGenerator(
    private val operator: Split,
    info: OperatorGenerationInfo
) : TypeAndShapeAwareOperatorGenerator(operator, info) {

    private val axis: Int = operator.getAttributeOrNull<Number>("axis")?.toInt() ?: 0
    private val split: IntArray? = operator.getAttributeOrNull<LongArray>("split")?.toIntArray()

    override fun resultInfo(): Map<String, TensorInfo> {
        if (split == null) {
            TODO("Split with unspecified part sizes is not supported yet")
        }
        val (inputShape, dataType) = inputInfo.first()
        val actualAxis = inputShape.actualAxis(axis)
        return operator.outputs.mapIndexed { index, output ->
            output to TensorInfo(
                shape = let {
                    val newShape = inputShape.copyOf()
                    newShape[actualAxis] = split[index]
                    newShape
                },
                dataType = dataType
            )
        }.toMap()
    }
}
