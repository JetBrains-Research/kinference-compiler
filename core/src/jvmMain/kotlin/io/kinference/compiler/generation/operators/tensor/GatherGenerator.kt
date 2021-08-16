package io.kinference.compiler.generation.operators.tensor

import io.kinference.compiler.generation.info.TensorInfo
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.operators.common.TypeAndShapeAwareOperatorGenerator
import io.kinference.compiler.generation.utils.*
import io.kinference.ndarray.Strides
import io.kinference.operators.tensor.Gather
import kotlin.time.ExperimentalTime

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

            inputs.forEach { (name, input) ->
                if (!input.shape.hasOneBlockInRow()) {
                    addLine("val ${name}BlocksInRow = ${input.shape.last()} / ${name}.array.blockSize")
                }
            }
            generateNestedLoops({ "i$it" }, indicesStrides.shape.map { 0 to it }) {
                generateLoop("iBlock", 0, "indicesBlocksInRow", toGenerate = !indices.shape.hasOneBlockInRow()) {
                    addLine(
                        "val indicesBlock = indicesBlocks[${
                            blockIndex(
                                indicesStrides,
                                mainIndices = { "i$it" },
                                blocksInRow = "indicesBlocksInRow",
                                offset = "iBlock",
                                oneBlockInRow = indices.shape.hasOneBlockInRow()
                            )
                        }]"
                    )
                    generateLoop("iIdx", 0, "indices.array.blockSize") {
                        if (!indices.shape.hasOneBlockInRow()) {
                            addLine("val i${indices.shape.lastIndex} = iBlock * indices.array.blockSize + iIdx")
                        } else {
                            addLine("val i${indices.shape.lastIndex} = iIdx")
                        }

                        addLine("val k = indicesBlock[iIdx].toInt().let { if (it < 0) it + ${data.shape[actualAxis]} else it }")

                        if (actualAxis == data.shape.lastIndex && !data.shape.hasOneBlockInRow()) {
                            add(
                                """
                                |val kBlock = k / data.array.blockSize
                                |val kIdx = k - kBlock * data.array.blockSize
                                |""".trimIndent()
                            )
                        }

                        generateNestedLoops(
                            { "j${if (it < actualAxis) it else it + 1}" },
                            dataStrides.shape.filterIndexed { index, _ -> index != actualAxis }.map { 0 to it }
                        ) {
                            if (actualAxis == data.shape.lastIndex) {
                                addLine(
                                    "val dataBlock = dataBlocks[${
                                        blockIndex(
                                            dataStrides,
                                            mainIndices = { "j$it" },
                                            blocksInRow = "dataBlocksInRow",
                                            offset = "kBlock",
                                            oneBlockInRow = data.shape.hasOneBlockInRow()
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
                                            blocksInRow = "indicesBlocksInRow",
                                            offset = "iBlock",
                                            oneBlockInRow = indices.shape.hasOneBlockInRow()
                                        )
                                    }]"
                                )
                                add("resultBlock[iIdx] = dataBlock[${if (data.shape.hasOneBlockInRow()) "k" else "kIdx"}]")
                            } else {
                                generateLoop("jBlock", 0, "dataBlockInRow", toGenerate = !data.shape.hasOneBlockInRow()) {
                                    addLine(
                                        "val dataBlock = dataBlocks[${
                                            blockIndex(
                                                dataStrides,
                                                mainIndices = { if (it != actualAxis) "j$it" else "k" },
                                                blocksInRow = "dataBlocksInRow",
                                                offset = "jBlock",
                                                oneBlockInRow = data.shape.hasOneBlockInRow()
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
                                                    else -> "j${it - indices.shape.size}"
                                                } },
                                                blocksInRow = "dataBlocksInRow",
                                                offset = "jBlock",
                                                oneBlockInRow = data.shape.hasOneBlockInRow()
                                            )
                                        }]"
                                    )
                                    generateLoop("jIdx", 0, "data.array.blockSize") {
                                        addLine("resultBlock[jIdx] = dataBlock[jIdx]")
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
