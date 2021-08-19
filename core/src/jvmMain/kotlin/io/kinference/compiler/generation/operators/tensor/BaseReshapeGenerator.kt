package io.kinference.compiler.generation.operators.tensor

import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.operators.common.TypeAndShapeAwareOperatorGenerator
import io.kinference.compiler.generation.utils.addLine
import io.kinference.compiler.generation.utils.mutableNDArrayTypeName
import io.kinference.ndarray.Strides
import io.kinference.operators.Operator
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
abstract class BaseReshapeGenerator(
    private val operator: Operator<*, *>,
    info: OperatorGenerationInfo
) : TypeAndShapeAwareOperatorGenerator(operator, info) {
    protected abstract val inputIndex: Int

    override fun generateImplInferred() {
        builder.apply {
            val (resultShape, resultType) = resultInfo().values.firstOrNull() ?: let {
                super.generateImplInferred()
                return@generateImplInferred
            }

            add("${nameMapping(operator.outputs.last())} = ")
            if (isLastUsage(inputIndex)) {
                add(
                    "%T(array = input$inputIndex.array, strides = input$inputIndex.strides)",
                    resultType.mutableNDArrayTypeName()
                )
            } else {
                add("input$inputIndex.toMutable()")
            }
            addLine(
                ".reshape(strides = %T(shape = intArrayOf(${resultShape.joinToString()})))",
                Strides::class
            )
        }
    }
}
