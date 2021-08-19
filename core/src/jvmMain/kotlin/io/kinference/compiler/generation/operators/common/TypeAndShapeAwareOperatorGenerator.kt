package io.kinference.compiler.generation.operators.common

import io.kinference.compiler.generation.info.TensorInfo
import io.kinference.compiler.generation.operators.BaseOperatorGenerator
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.utils.*
import io.kinference.operators.Operator
import kotlin.time.ExperimentalTime

/* Wrapper for generators based on full type and shape information.
 * If type and shape are not inferred, it generates default implementation.
 */
@OptIn(ExperimentalTime::class)
abstract class TypeAndShapeAwareOperatorGenerator(
    private val operator: Operator<*, *>,
    info: OperatorGenerationInfo
) : BaseOperatorGenerator(info) {
    protected open val inputsToInfer: List<String> = operator.inputs

    protected val inputInfo: List<TensorInfo>
        get() = operator.inputs.map { tensorInfo.getValue(it) }

    /* Implementation for known input types and shapes. */
    protected open fun generateImplInferred() = generateImplDefault()

    /* Default implementation if exact inputs types and shapes are unknown. */
    private fun generateImplDefault() {
        builder.add(DefaultOperatorGenerator(operator, info).generate())
    }

    protected abstract fun resultInfo(): Map<String, TensorInfo>

    override fun generateImpl() {
        builder.apply {
            if (inputsToInfer.all { tensorInfo.containsKey(it) }) {
                operator.inputs.forEachIndexed { index, input ->
                    addLine("val input$index = %L // %L", nameMapping(input), input)
                }

                resultInfo().forEach { (name, info) ->
                    tensorInfo[name] = info
                }

                val inputs = operator.inputs.withIndex().mapNotNull { (index, input) ->
                    tensorInfo[input]?.let { index to it }
                }
                addNamedLine(
                    "require(${inputs.joinToString(separator = " && ") { (index, input) ->
                        "input$index is %inputType$index:T${input.dataType.questionMark()}"
                    }})",
                    inputs.associate { (index, input) ->
                        "inputType$index" to input.dataType.ndArrayTypeName()
                    }
                )
                inputs.forEach { (index, input) ->
                    addLine("require(input$index.shape.contentEquals(intArrayOf(${input.shape.joinToString()})))")
                }
                endLine()
                generateImplInferred()
            } else {
                generateImplDefault()
            }
        }
    }
}
