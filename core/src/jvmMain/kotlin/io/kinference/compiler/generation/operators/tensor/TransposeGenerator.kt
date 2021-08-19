package io.kinference.compiler.generation.operators.tensor

import io.kinference.compiler.generation.info.TensorInfo
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.operators.common.TypeAndShapeAwareOperatorGenerator
import io.kinference.compiler.generation.utils.*
import io.kinference.ndarray.Strides
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

    override fun generateImplInferred() {
        builder.apply {
            val input = inputInfo.first()
            val actualPerm = actualPerm(input.shape)
            val (resultShape, resultType) = resultInfo().values.first()

            val (inputStrides, resultStrides) = listOf(input.shape, resultShape).map {
                Strides(shape = it.dropLast(1).toIntArray())
            }

            addLine("val inputBlocks = input0.array.blocks")

            val transposeByBlocks = actualPerm.last() == input.shape.lastIndex
            add("val resultBlocks = Array(${resultShape.blocksNum()}) ")
            if (transposeByBlocks && isLastUsage(0)) {
                addLine("{ ${resultType.funArrayOf()}() }")
            } else {
                addLine("{ ${resultType.arrayTypeName()}(${resultShape.blockSize()}) }")
            }

            val loopIndices = IndexStorage()

            if (transposeByBlocks) {
                generateNestedLoops({ "i$it" }, inputStrides.shape.map { 0 to it }, loopIndices) {
                    generateLoop("block", 0 to resultShape.blocksInRow(), loopIndices) {
                        add("resultBlocks[${
                            blockIndex(
                                resultStrides,
                                mainIndices = { "i${actualPerm[it]}" }, 
                                blockOffset = "block", 
                                indexStorage = loopIndices
                            )
                        }] = inputBlocks[${
                            blockIndex(
                                inputStrides,
                                mainIndices = { "i$it" },
                                blockOffset = "block",
                                indexStorage = loopIndices
                            )
                        }]")
                        if (isLastUsage(0)) {
                            endLine()
                        } else {
                            addLine(".copyOf()")
                        }
                    }
                }
            } else {
                generateNestedLoops({ "i$it" }, inputStrides.shape.take(1 + actualPerm.last()).map { 0 to it }, loopIndices) {
                    if (resultShape.blocksInRow() > 1) {
                        add(
                            """
                            |val resultBlock = i${actualPerm.last()} / ${resultShape.blockSize()}
                            |val resultIdx = i${actualPerm.last()} - resultBlock * ${resultShape.blockSize()}
                            |""".trimIndent()
                        )
                    }
                    loopIndices.put("resultBlock", 0 to resultShape.blocksInRow())
                    loopIndices.put("resultIdx", 0 to resultShape.blockSize())

                    generateNestedLoops({ "i${it + actualPerm.last() + 1}" }, inputStrides.shape.drop(1 + actualPerm.last()).map { 0 to it }, loopIndices) {
                        generateLoop("block", 0 to input.shape.blocksInRow(), loopIndices) {
                            addLine("val inputBlock = inputBlocks[${
                                blockIndex(
                                    inputStrides,
                                    { "i$it" },
                                    "block",
                                    loopIndices
                                )
                            }]")
                            generateLoop("idx", 0 to input.shape.blockSize(), loopIndices) {
                                val lastIndex = loopIndices.inlineLiteral("block") * input.shape.blockSize().toLiteral() + loopIndices.inlineLiteral("idx")
                                addLine("val i${input.shape.lastIndex} = $lastIndex")
                                loopIndices.put("i${input.shape.lastIndex}", 0 to resultShape.last())

                                val resultIdx = if (resultShape.blocksInRow() > 1) {
                                    loopIndices.inlineLiteral("resultIdx")
                                } else {
                                    loopIndices.inlineLiteral("i${actualPerm.last()}")
                                }

                                addLine("resultBlocks[${
                                    blockIndex(
                                        resultStrides,
                                        { "i${actualPerm[it]}" },
                                        "resultBlock",
                                        loopIndices
                                    )
                                }][$resultIdx] = inputBlock[${loopIndices.inline("idx")}]")
                            }
                        }
                    }
                }
            }

            val output = operator.outputs.first()
            addLine(
                "%L = %T(array = %T(blocks = resultBlocks), strides = %T(shape = intArrayOf(${resultShape.joinToString()}))) // %L",
                nameMapping(output),
                resultType.ndArrayTypeName(),
                resultType.tiledArrayTypeName(),
                Strides::class,
                output
            )
        }
    }
}
