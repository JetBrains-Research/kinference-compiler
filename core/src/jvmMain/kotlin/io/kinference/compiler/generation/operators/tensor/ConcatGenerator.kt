package io.kinference.compiler.generation.operators.tensor

import io.kinference.compiler.generation.info.TensorInfo
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.operators.common.TypeAndShapeAwareOperatorGenerator
import io.kinference.compiler.generation.utils.*
import io.kinference.ndarray.Strides
import io.kinference.operators.tensor.Concat
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class ConcatGenerator(
    private val operator: Concat,
    info: OperatorGenerationInfo
) : TypeAndShapeAwareOperatorGenerator(operator, info) {

    private val axis = operator.getAttribute<Number>("axis").toInt()

    override fun generateImplInferred() {
        builder.apply {
            val (resultShape, resultType) = resultInfo().values.first()
            val actualAxis = resultShape.actualAxis(axis)

            inputInfo.indices.forEach { index ->
                addLine("val input${index}Blocks = input${index}.array.blocks")
            }
            addLine(
                """
                |val resultStrides = %T(shape = intArrayOf(${resultShape.joinToString()}))
                |val resultArray = %T(strides = resultStrides)
                |""".trimMargin(),
                Strides::class,
                resultType.tiledArrayTypeName()
            )

            inputInfo.forEachIndexed { index, input ->
                if (!input.shape.hasOneBlockInRow()) {
                    addLine("val input${index}BlocksInRow = ${input.shape.last()} / input${index}.array.blockSize")
                }
            }
            if (actualAxis == resultShape.lastIndex) {
                addLine("val resultPointer = resultArray.pointer()")
                generateLoop("i", 0, "resultArray.blocksNum") {
                    inputInfo.forEachIndexed { index, input ->
                        generateLoop("j", 0, "input${index}BlocksInRow", toGenerate = !input.shape.hasOneBlockInRow()) {
                            val blockIndex = if (!input.shape.hasOneBlockInRow()) "i * input${index}BlocksInRow + j" else "i"
                            addLine("val input${index}Block = input${index}Blocks[${blockIndex}]")
                            generateLoop("k", 0, "input${index}.array.blockSize") {
                                add(
                                    """
                                    |resultPointer.set(input${index}Block[k])
                                    |resultPointer.increment()
                                    |""".trimMargin())
                            }
                        }
                    }
                }
            } else {
                TODO()
            }
            val output = operator.outputs.first()
            addLine(
                "%L = %T(array = resultArray, strides = resultStrides) // %L",
                nameMapping(output),
                resultType.ndArrayTypeName(),
                output
            )
        }
    }

    override fun resultInfo(): Map<String, TensorInfo> {
        val resultShape = inputInfo.first().shape.copyOf()
        val actualAxis = resultShape.actualAxis(axis)
        resultShape[actualAxis] = inputInfo.sumOf { it.shape[actualAxis] }

        val resultType = inputInfo.first().dataType

        return mapOf(operator.outputs.first() to TensorInfo(shape = resultShape, dataType = resultType))
    }
}
