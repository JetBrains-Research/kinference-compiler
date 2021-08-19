package io.kinference.compiler.generation.operators.tensor

import io.kinference.compiler.generation.info.TensorInfo
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.operators.common.TypeAndShapeAwareOperatorGenerator
import io.kinference.compiler.generation.utils.*
import io.kinference.ndarray.Strides
import io.kinference.operators.tensor.Concat
import kotlin.time.ExperimentalTime

/**
 * Concat generator.
 *
 * [ONNX documentation](https://github.com/onnx/onnx/blob/master/docs/Operators.md#Concat)
 *
 * KInference class: [Concat]
 */
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

            val loopIndices = IndexStorage()

            if (actualAxis != resultShape.lastIndex) {
                addLine("val resultBlocks = resultArray.blocks")
                inputInfo.forEachIndexed { index, input ->
                    val matrixPartSize =
                        input.shape.slice(actualAxis until input.shape.lastIndex).fold(1, Int::times)
                    addLine("val input${index}MatrixPartSize = ${matrixPartSize * input.shape.blocksInRow()}")
                }
                addLine("var resultBlockIndex = 0")
                val numIterations = resultShape.slice(0 until actualAxis).fold(1, Int::times)
                generateLoop("i", 0 to numIterations, loopIndices) {
                    inputInfo.indices.forEach { index ->
                        val inputOffset = loopIndices.inlineLiteral("i") * "input${index}MatrixPartSize".toLiteral()
                        addLine("val input${index}Offset = $inputOffset")
                        generateLoop(
                            "j",
                            "input${index}Offset" to "input${index}Offset + input${index}MatrixPartSize",
                            loopIndices
                        ) {
                            if (isLastUsage(index)) {
                                addLine("resultBlocks[resultBlockIndex++] = input${index}Blocks[j]")
                            } else {
                                addLine("input${index}Blocks[j].copyInto(resultBlocks[resultBlockIndex++])")
                            }
                        }
                    }
                }
            } else {
                if (resultShape.blocksInRow() > 1) {
                    addLine("val resultPointer = resultArray.pointer()")
                    generateLoop("i", 0 to resultShape.blocksNum(), loopIndices) {
                        inputInfo.forEachIndexed { index, input ->
                            generateLoop("j", 0 to input.shape.blocksInRow(), loopIndices) {
                                val blockIndex = loopIndices.inlineLiteral("i") * input.shape.blocksInRow()
                                    .toLiteral() + loopIndices.inlineLiteral("j").toLiteral()
                                addLine("val input${index}Block = input${index}Blocks[${blockIndex}]")
                                generateLoop("k", 0 to input.shape.blockSize(), loopIndices) {
                                    add(
                                        """
                                        |resultPointer.set(input${index}Block[${loopIndices.inlineLiteral("k")}])
                                        |resultPointer.increment()
                                        |""".trimMargin()
                                    )
                                }
                            }
                        }
                    }
                } else {
                    addLine("val resultBlocks = resultArray.blocks")
                    generateLoop("i", 0 to resultShape.blocksNum(), loopIndices) {
                        val blockIndex = loopIndices.inlineLiteral("i")
                        addLine("val resultBlock = resultBlocks[$blockIndex]")
                        addLine("var idx = 0")
                        inputInfo.forEachIndexed { index, input ->
                            addLine("val input${index}Block = input${index}Blocks[$blockIndex]")
                            generateLoop("k", 0 to input.shape.blockSize(), loopIndices) {
                                addLine("resultBlock[idx++] = input${index}Block[${loopIndices.inlineLiteral("k")}]")
                            }
                        }
                    }
                }
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
