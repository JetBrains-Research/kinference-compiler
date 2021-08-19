package io.kinference.compiler.generation.operators.math

import com.squareup.kotlinpoet.CodeBlock
import io.kinference.compiler.generation.info.TensorInfo
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.operators.common.TypeAndShapeAwareOperatorGenerator
import io.kinference.compiler.generation.utils.*
import io.kinference.ndarray.Strides
import io.kinference.ndarray.broadcasting.Broadcasting
import io.kinference.ndarray.broadcasting.unsqueezeFirst
import io.kinference.operators.math.MatMul
import kotlin.time.ExperimentalTime

/**
 * MatMul generator.
 *
 * [ONNX documentation](https://github.com/onnx/onnx/blob/master/docs/Operators.md#MatMul)
 *
 * KInference class: [MatMul]
 */
@OptIn(ExperimentalTime::class)
class MatMulGenerator(
    private val operator: MatMul,
    info: OperatorGenerationInfo
) : TypeAndShapeAwareOperatorGenerator(operator, info) {
    override fun generateImplInferred() {
        builder.apply {
            val (resultShape, resultType) = resultInfo().values.first()
            val inputShapes = inputInfo.map { unsqueezeFirst(it.shape, resultShape.size) }

            val inputOuterShapes = inputShapes.map { it.dropLast(2).toIntArray() }
            val inputMatrixShapes = inputShapes.map { it.takeLast(2).toIntArray() }

            val resultOuterShape = resultShape.dropLast(2).toIntArray()
            val resultMatrixShape = resultShape.takeLast(2).toIntArray()

            val inputStrides = inputOuterShapes.map { Strides(shape = it) }
            val resultStrides = Strides(shape = resultOuterShape)

            add(
                """
                |val resultStrides = %T(shape = intArrayOf(${resultShape.joinToString()}))
                |val result = %T(array = %T(strides = resultStrides), strides = resultStrides)
                |
                |""".trimMargin(),
                Strides::class,
                resultType.mutableNDArrayTypeName(),
                resultType.tiledArrayTypeName()
            )

            if (resultShape.size == 2) {
                addLine("input0.dot(input1, result)")
            } else {
                val blocksInMatrix: (arrayName: String, shape: IntArray) -> Unit = { arrayName, shape ->
                    addLine("val ${arrayName}BlocksInMatrix = ${shape[0] * shape[1] / shape.blockSize()}")
                }

                inputMatrixShapes.forEachIndexed { index, shape ->
                    blocksInMatrix("input$index", shape)
                }
                blocksInMatrix("result", resultMatrixShape)
                endLine()

                operator.inputs.indices.forEach { index ->
                    addLine("val input${index}Blocks = input$index.array.blocks")
                }
                addLine("val resultBlocks = result.array.blocks")
                endLine()

                val loopIndices = IndexStorage()

                generateNestedLoops({ "i$it" }, resultOuterShape.map { 0 to it }, loopIndices) {
                    inputStrides.forEachIndexed { index, strides ->
                        matrixSlice("input$index", strides, inputMatrixShapes[index], resultType)
                    }
                    matrixSlice("result", resultStrides, resultMatrixShape, resultType)
                    addLine("input0Matrix.dot(input1Matrix, resultMatrix)")
                }
            }
            val output = operator.outputs.first()
            addLine(
                "%L = result // %L",
                nameMapping(output),
                output
            )
        }
    }

    override fun resultInfo(): Map<String, TensorInfo> {
        val actualInputShapes = inputInfo.map { it.shape }
        if (actualInputShapes.any { it.size < 2 }) {
            TODO()
        }
        val resultShape = Broadcasting.broadcastShapeForMatmul(actualInputShapes[0], actualInputShapes[1])

        val resultType = inputInfo.first().dataType

        return mapOf(operator.outputs.first() to TensorInfo(shape = resultShape, dataType = resultType))
    }

    private fun matrixOffset(strides: Strides, blocksInMatrix: String): String {
        val indexedStrides = strides.shape.withIndex().filter { it.value != 1 }.map { (index, _) ->
            index to strides.strides[index]
        }
        return (indexedStrides.map { (index, stride) ->
            "i$index".toLiteral() * stride.toLiteral()
        }.toAddExpression() * blocksInMatrix.toLiteral()).toString()
    }

    private fun CodeBlock.Builder.matrixSlice(
        arrayName: String,
        strides: Strides,
        matrixShape: IntArray,
        type: DataTypeInfo
    ) {
        add(
            """
            |val ${arrayName}MatrixOffset = ${matrixOffset(strides, "${arrayName}BlocksInMatrix")}
            |val ${arrayName}Matrix = %T(
            |    array = %T(
            |        blocks = ${arrayName}Blocks.sliceArray(
            |            ${arrayName}MatrixOffset until ${arrayName}MatrixOffset + ${arrayName}BlocksInMatrix
            |        )
            |    ),
            |    strides = %T(shape = intArrayOf(${matrixShape.joinToString()}))
            |)
            |""".trimMargin(),
            type.mutableNDArrayTypeName(),
            type.tiledArrayTypeName(),
            Strides::class
        )
    }
}
