package io.kinference.compiler.generation.operators.tensor

import io.kinference.compiler.generation.info.TensorInfo
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.operators.common.TypeAndShapeAwareOperatorGenerator
import io.kinference.ndarray.Strides
import io.kinference.ndarray.arrays.LongNDArray
import io.kinference.ndarray.toIntArray
import io.kinference.operators.tensor.Reshape
import kotlin.time.ExperimentalTime

/**
 * Reshape generator.
 *
 * [ONNX documentation](https://github.com/onnx/onnx/blob/master/docs/Operators.md#Reshape)
 *
 * KInference class: [Reshape]
 */
@OptIn(ExperimentalTime::class)
class ReshapeGenerator(
    private val operator: Reshape,
    info: OperatorGenerationInfo
) : BaseReshapeGenerator(operator, info) {
    override val inputIndex: Int = 0

    override fun resultInfo(): Map<String, TensorInfo> {
        val (input, targetShape) = inputInfo
        if (targetShape.tensor == null) {
            return emptyMap()
        }
        val shape = input.shape
        val newShape = (targetShape.tensor.data as LongNDArray).array.toArray().toIntArray()
        for ((i, axisShape) in newShape.withIndex()) {
            if (axisShape == 0) newShape[i] = shape[i]
        }

        val negativeIdx = newShape.indexOf(-1)
        if (negativeIdx != -1) {
            val elementsCount = newShape.filter { it != -1 }.fold(1, Int::times)
            newShape[negativeIdx] = Strides(shape).linearSize / elementsCount
        }

        return mapOf(operator.outputs.first() to TensorInfo(
            shape = newShape,
            dataType = input.dataType
        ))
    }
}
