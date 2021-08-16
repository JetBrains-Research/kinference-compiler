package io.kinference.compiler.generation.operators.tensor

import io.kinference.compiler.generation.info.TensorInfo
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.operators.common.TypeAndShapeAwareOperatorGenerator
import io.kinference.compiler.generation.utils.actualAxis
import io.kinference.operators.tensor.Flatten
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class FlattenGenerator(
    private val operator: Flatten,
    info: OperatorGenerationInfo
) : TypeAndShapeAwareOperatorGenerator(operator, info) {

    private val axis = operator.getAttribute<Number>("axis").toInt()

    override fun resultInfo(): Map<String, TensorInfo> {
        val (shape, dataType) = inputInfo.first()
        val actualAxis = shape.actualAxis(axis)

        val firstDimension = shape.slice(0 until actualAxis).fold(1, Int::times)
        val secondDimension = shape.slice(actualAxis until shape.size).fold(1, Int::times)

        return mapOf(
            operator.outputs.first() to TensorInfo(
                shape = intArrayOf(firstDimension, secondDimension),
                dataType = dataType
            )
        )
    }
}
