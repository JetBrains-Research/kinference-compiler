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

    protected abstract fun resultType(inputType: DataTypeInfo): DataTypeInfo

    override fun generateImplInferred() {
        builder.apply {
            val inputInfo = operator.inputs.map { tensorInfo.getValue(it) }

            val actualInputShapes = inputInfo.map { it.shape }
            val resultShape = Broadcasting.broadcastShape(actualInputShapes)
            val inputShapes = actualInputShapes.map { unsqueezeFirst(it, resultShape.size) }

            val inputStrides = inputShapes.map { Strides(shape = it.dropLast(1).toIntArray()) }
            val resultStrides = Strides(shape = resultShape.dropLast(1).toIntArray())

            val inputType = inputInfo[0].dataType

            operator.inputs.indices.forEach { index ->
                addLine("val inputBlocks$index = input$index.array.blocks")
            }
            endLine()

            addLine(
                """
                |val resultStrides = %T(shape = intArrayOf(${resultShape.joinToString()}))
                |val resultArray = %T(strides = resultStrides)
                |val resultBlocks = resultArray.blocks
                |""".trimMargin(),
                Strides::class,
                resultType(inputType).tiledArrayTypeName()
            )

            if (resultShape.isEmpty()) {
                addLine(operatorImpl(
                    operator.inputs.indices.map { index -> "inputBlocks$index[0][0]" },
                    "resultBlocks[0][0]",
                    resultType(inputType)
                ))
            } else {
                val oneBlockInRow = resultShape.hasOneBlockInRow()
                if (!resultShape.hasOneBlockInRow()) {
                    addLine("val blocksInRow = resultStrides.shape.last() / resultArray.blockSize")
                }
                generateNestedLoops({ "i$it" }, resultStrides.shape.map { 0 to it }) {
                    generateLoop("j", 0, "blocksInRow", toGenerate = !oneBlockInRow) {
                        inputStrides.forEachIndexed { index, strides ->
                            val oneBlockInInputRow = oneBlockInRow || inputShapes[index].last() == 1
                            addLine("val inputBlock$index = inputBlocks$index[${blockIndex(
                                strides, { "i$it" }, "blocksInRow", "j", oneBlockInInputRow
                            )}]")
                        }
                        addLine("val resultBlock = resultBlocks[${blockIndex(
                            resultStrides, { "i$it" }, "blocksInRow", "j", oneBlockInRow
                        )}]")

                        generateLoop("k", 0, "resultArray.blockSize") {
                            val inputIndices = inputShapes.map { if (it.last() == 1) "0" else "k" }
                            addLine(operatorImpl(
                                inputIndices.mapIndexed { index, inputIndex -> "inputBlock$index[$inputIndex]" },
                                "resultBlock[k]",
                                resultType(inputType)
                            ))
                        }
                    }
                }
            }
            val output = operator.outputs[0]
            addLine(
                "%L = %T(array = resultArray, strides = resultStrides) // %L",
                nameMapping(output),
                resultType(inputType).ndArrayTypeName(),
                output
            )

            tensorInfo[output] = TensorInfo(shape = resultShape, dataType = resultType(inputType))
        }
    }

    override fun resultInfo(): Map<String, TensorInfo> {
        val actualInputShapes = inputInfo.map { it.shape }
        val resultShape = Broadcasting.broadcastShape(actualInputShapes)

        val resultType = resultType(inputInfo.first().dataType)

        return mapOf(operator.outputs.first() to TensorInfo(shape = resultShape, dataType = resultType))
    }
}
