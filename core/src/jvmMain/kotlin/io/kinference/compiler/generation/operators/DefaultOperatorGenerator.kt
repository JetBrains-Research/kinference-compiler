package io.kinference.compiler.generation.operators

import com.squareup.kotlinpoet.MemberName
import io.kinference.graph.Context
import io.kinference.operators.Operator

class DefaultOperatorGenerator(
    private val operator: Operator<*, *>,
    nameMapping: (String) -> String
) : BaseOperatorGenerator(nameMapping) {
    override fun generateImpl() {
        builder.apply {
            // Operator definition
            // TODO attributes
            add(
                """
                |val operator = %T(
                |    attributes = mapOf(),
                |    inputs = listOf(${operator.inputs.joinToString { "\"$it\"" }}),
                |    outputs = listOf(${operator.outputs.joinToString { "\"$it\"" }}),
                |)
                |""".trimMargin(),
                operator::class
            )

            // Input tensors
            // TODO ONNXSequence, ONNXMap
            add("val inputs = listOf(\n")
            indent()
            operator.inputs.forEach {
                add(
                    "${nameMapping(it)}.%M(%S),\n",
                    MemberName("io.kinference.data.tensors", "asTensor", isExtension = true),
                    it
                )
            }
            unindent()
            add(")\n")

            // Apply operator
            addStatement("val result = operator.apply(%T(), inputs)", Context::class)
            addStatement("val resultMap = operator.outputs.zip(result.map { it?.data }).toMap()")

            // Extract outputs
            operator.outputs.forEach {
                addStatement(
                    "${nameMapping(it)} = resultMap[%S] ?: error(%S)",
                    it,
                    "Required output '$it' not provided by '${operator.info.name}' operator"
                )
            }
        }
    }
}
