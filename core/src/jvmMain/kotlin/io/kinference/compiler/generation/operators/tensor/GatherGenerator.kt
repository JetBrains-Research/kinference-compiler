package io.kinference.compiler.generation.operators.tensor

import io.kinference.compiler.generation.info.TensorInfo
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.operators.common.TypeAndShapeAwareOperatorGenerator
import io.kinference.compiler.generation.utils.*
import io.kinference.ndarray.Strides
import io.kinference.operators.tensor.Gather
import kotlin.time.ExperimentalTime

/**
 * Gather generator.
 *
 * [ONNX documentation](https://github.com/onnx/onnx/blob/master/docs/Operators.md#Gather)
 *
 * KInference class: [Gather]
 */
@OptIn(ExperimentalTime::class)
class GatherGenerator(
    private val operator: Gather,
    info: OperatorGenerationInfo
) : TypeAndShapeAwareOperatorGenerator(operator, info) {

    private val axis = operator.getAttribute<Number>("axis").toInt()

    override fun generateImplInferred() {
        builder.apply {
            val (data, indices) = inputInfo
            val inputs = listOf("data" to data, "indices" to indices)

            val actualAxis = data.shape.actualAxis(axis)
            val (resultShape, resultType) = resultInfo().values.first()

            val inputStrides = inputs.map { (name, input) -> name to Strides(shape = input.shape.dropLast(1).toIntArray()) }
            val (dataStrides, indicesStrides) = inputStrides.map { it.second }
            val resultStrides = Strides(shape = resultShape.dropLast(1).toIntArray())

            inputs.forEachIndexed { index, (name, _) ->
                addLine(
                    """
                    |val $name = input$index
                    |val ${name}Blocks = $name.array.blocks
                    |""".trimMargin()
                )
            }
            addLine(
                """
                |val resultStrides = %T(shape = intArrayOf(${resultShape.joinToString()}))
                |val resultArray = %T(strides = resultStrides)
                |val resultBlocks = resultArray.blocks
                |""".trimMargin(),
                Strides::class,
                resultType.tiledArrayTypeName()
            )

            val loopIndices = IndexStorage()

            generateNestedLoops({ "i$it" }, indicesStrides.shape.map { 0 to it }, loopIndices) {
                generateLoop("iBlock", 0 to indices.shape.blocksInRow(), loopIndices) {
                    addLine(
                        "val indicesBlock = indicesBlocks[${
                            blockIndex(
                                indicesStrides,
                                mainIndices = { "i$it" },
                                blockOffset = "iBlock",
                                indexStorage = loopIndices
                            )
                        }]"
                    )
                    generateLoop("iIdx", 0 to indices.shape.blockSize(), loopIndices) {
                        val lastIndex = loopIndices.inlineLiteral("iBlock") * indices.shape.blockSize().toLiteral() + loopIndices.inlineLiteral("iIdx")
                        loopIndices.put("i${indices.shape.lastIndex}", 0 to lastIndex.toString())
                        addLine("val i${indices.shape.lastIndex} = $lastIndex")
                        addLine("val k = indicesBlock[${loopIndices.inline("iIdx")}].toInt().let { if (it < 0) it + ${data.shape[actualAxis]} else it }")
                        if (actualAxis == data.shape.lastIndex && data.shape.blocksInRow() > 1) {
                            add(
                                """
                                |val kBlock = k / ${data.shape.blockSize()}
                                |val kIdx = k - kBlock * ${data.shape.blockSize()}
                                |""".trimIndent()
                            )
                        }
                        loopIndices.put("k", 0 to data.shape.last())
                        loopIndices.put("kBlock", 0 to data.shape.blocksInRow())
                        loopIndices.put("kIdx", 0 to data.shape.blockSize())

                        generateNestedLoops(
                            { "j${if (it < actualAxis) it else it + 1}" },
                            dataStrides.shape.filterIndexed { index, _ -> index != actualAxis }.map { 0 to it },
                            loopIndices
                        ) {
                            if (actualAxis == data.shape.lastIndex) {
                                addLine(
                                    "val dataBlock = dataBlocks[${
                                        blockIndex(
                                            dataStrides,
                                            mainIndices = { "j$it" },
                                            blockOffset = "kBlock",
                                            indexStorage = loopIndices
                                        )
                                    }]"
                                )
                                addLine(
                                    "val resultBlock = resultBlocks[${
                                        blockIndex(
                                            resultStrides,
                                            mainIndices = { when {
                                                it < actualAxis -> "j$it"
                                                else -> "i${it - actualAxis}"
                                            } },
                                            blockOffset = "iBlock",
                                            indexStorage = loopIndices
                                        )
                                    }]"
                                )
                                add("resultBlock[iIdx] = dataBlock[${if (data.shape.blocksInRow() == 1) "k" else "kIdx"}]")
                            } else {
                                generateLoop("jBlock", 0 to data.shape.blocksInRow(), loopIndices) {
                                    addLine(
                                        "val dataBlock = dataBlocks[${
                                            blockIndex(
                                                dataStrides,
                                                mainIndices = { if (it != actualAxis) "j$it" else "k" },
                                                blockOffset = "jBlock",
                                                indexStorage = loopIndices
                                            )
                                        }]"
                                    )
                                    addLine(
                                        "val resultBlock = resultBlocks[${
                                            blockIndex(
                                                resultStrides,
                                                mainIndices = { when {
                                                    it < actualAxis -> "j$it"
                                                    it - actualAxis < indices.shape.size -> "i${it - actualAxis}"
                                                    else -> "j${it - indices.shape.size + 1}"
                                                } },
                                                blockOffset = "jBlock",
                                                indexStorage = loopIndices
                                            )
                                        }]"
                                    )
                                    generateLoop("jIdx", 0 to data.shape.blockSize(), loopIndices) {
                                        addLine("resultBlock[${loopIndices.inline("jIdx")}] = dataBlock[${loopIndices.inline("jIdx")}]")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            val output = operator.outputs[0]
            addLine(
                "%L = %T(array = resultArray, strides = resultStrides) // %L",
                nameMapping(output),
                resultType.ndArrayTypeName(),
                output
            )

            tensorInfo[output] = TensorInfo(shape = resultShape, dataType = resultType)
        }
    }

    override fun resultInfo(): Map<String, TensorInfo> {
        val (data, indices) = inputInfo
        val actualAxis = if (axis < 0) data.shape.size + axis else axis

        val resultShape = data.shape.slice(0 until actualAxis).toIntArray() +
                indices.shape + data.shape.slice(actualAxis + 1 until data.shape.size).toIntArray()

        val resultType = inputInfo.first().dataType

        return mapOf(operator.outputs.first() to TensorInfo(shape = resultShape, dataType = resultType))
    }
}
