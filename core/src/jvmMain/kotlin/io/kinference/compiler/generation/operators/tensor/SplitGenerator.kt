package io.kinference.compiler.generation.operators.tensor

import io.kinference.compiler.generation.info.TensorInfo
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.operators.common.TypeAndShapeAwareOperatorGenerator
import io.kinference.compiler.generation.utils.actualAxis
import io.kinference.ndarray.toIntArray
import io.kinference.operators.tensor.Split
import kotlin.math.ceil
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class SplitGenerator(
    private val operator: Split,
    info: OperatorGenerationInfo
) : TypeAndShapeAwareOperatorGenerator(operator, info) {

    private val axis: Int = operator.getAttributeOrNull<Number>("axis")?.toInt() ?: 0
    private val split: IntArray? = operator.getAttributeOrNull<LongArray>("split")?.toIntArray()

    override fun resultInfo(): Map<String, TensorInfo> {
        val (inputShape, dataType) = inputInfo.first()
        val actualAxis = inputShape.actualAxis(axis)

        val actualSplit = if (split == null) {
            // FIXME This implementation reproduces KInference behavior, but actually is not correct.
            val parts = operator.outputs.size
            val elementsByIndex = inputShape[actualAxis]
            val mainSplit = ceil(elementsByIndex.toDouble() / parts).toInt()
            val newSplit = IntArray(parts) { mainSplit }

            val tail = elementsByIndex % parts
            if (tail != 0) newSplit[parts - 1] = tail

            newSplit
        } else {
            split
        }
        return operator.outputs.mapIndexed { index, output ->
            output to TensorInfo(
                shape = let {
                    val newShape = inputShape.copyOf()
                    newShape[actualAxis] = actualSplit[index]
                    newShape
                },
                dataType = dataType
            )
        }.toMap()
    }
}
