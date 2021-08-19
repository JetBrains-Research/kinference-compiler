package io.kinference.compiler.generation.operators.common

import com.squareup.kotlinpoet.CodeBlock
import io.kinference.compiler.generation.info.TensorInfo
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.utils.*
import io.kinference.ndarray.Strides
import io.kinference.ndarray.broadcasting.Broadcasting
import io.kinference.ndarray.broadcasting.unsqueezeFirst
import io.kinference.operators.Operator
import kotlin.time.ExperimentalTime

/* Base class for generation of operators that support multidirectional broadcasting. */
@OptIn(ExperimentalTime::class)
abstract class MultidirectionalBroadcastingOperatorGenerator(
    protected val operator: Operator<*, *>,
    info: OperatorGenerationInfo
) : TypeAndShapeAwareOperatorGenerator(operator, info) {

    protected abstract fun operatorImpl(inputs: List<String>, output: String, resultType: DataTypeInfo): CodeBlock

    protected abstract val resultType: DataTypeInfo

    override fun generateImplInferred() {
        builder.apply {
            val actualInputShapes = inputInfo.map { it.shape }
            val resultShape = Broadcasting.broadcastShape(actualInputShapes)
            val inputShapes = actualInputShapes.map { unsqueezeFirst(it, resultShape.size) }

            val inputStrides = inputShapes.map { Strides(shape = it.dropLast(1).toIntArray()) }
            val resultStrides = Strides(shape = resultShape.dropLast(1).toIntArray())

            operator.inputs.indices.forEach { index ->
                addLine("val input${index}Blocks = input$index.array.blocks")
            }
            endLine()

            addLine(
                """
                |val resultStrides = %T(shape = intArrayOf(${resultShape.joinToString()}))
                |val resultArray = %T(strides = resultStrides)
                |val resultBlocks = resultArray.blocks
                |""".trimMargin(),
                Strides::class,
                resultType.tiledArrayTypeName()
            )

            if (resultShape.isEmpty()) {
                addLine(operatorImpl(
                    operator.inputs.indices.map { index -> "input${index}Blocks[0][0]" },
                    "resultBlocks[0][0]",
                    resultType
                ))
            } else {
                val loopIndices = IndexStorage()

                generateNestedLoops({ "i$it" }, resultStrides.shape.map { 0 to it }, loopIndices) {
                    generateLoop("block", 0 to resultShape.blocksInRow(), loopIndices) {
                        (inputStrides.mapIndexed { index, strides -> "input$index" to strides } + ("result" to resultStrides)).forEach { (name, strides) ->
                            addLine("val ${name}Block = ${name}Blocks[${
                                blockIndex(
                                    strides,
                                    mainIndices = { "i$it" },
                                    blockOffset = "block",
                                    indexStorage = loopIndices
                                )
                            }]")
                        }

                        generateLoop("idx", 0 to resultShape.blockSize(), loopIndices) {
                            val inputIndices = inputShapes.map { if (it.last() == 1) "0" else loopIndices.inline("idx") }
                            addLine(operatorImpl(
                                inputIndices.mapIndexed { index, inputIndex -> "input${index}Block[$inputIndex]" },
                                "resultBlock[${loopIndices.inline("idx")}]",
                                resultType
                            ))
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
        }
    }

    override fun resultInfo(): Map<String, TensorInfo> {
        val actualInputShapes = inputInfo.map { it.shape }
        val resultShape = Broadcasting.broadcastShape(actualInputShapes)

        return mapOf(operator.outputs.first() to TensorInfo(shape = resultShape, dataType = resultType))
    }
}
