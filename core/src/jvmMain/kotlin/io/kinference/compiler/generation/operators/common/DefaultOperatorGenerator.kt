package io.kinference.compiler.generation.operators.common

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.withIndent
import io.kinference.attributes.Attribute
import io.kinference.compiler.generation.models.ListInitBuilder
import io.kinference.compiler.generation.models.MapInitBuilder
import io.kinference.compiler.generation.operators.BaseOperatorGenerator
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.utils.addLine
import io.kinference.operators.Operator
import io.kinference.protobuf.message.AttributeProto
import kotlin.time.ExperimentalTime

/* Default generator for unimplemented operators. */
@OptIn(ExperimentalTime::class)
class DefaultOperatorGenerator(
    private val operator: Operator<*, *>,
    info: OperatorGenerationInfo
) : BaseOperatorGenerator(operator, info) {
    override fun generateImpl() {
        builder.apply {
            addLine("val operator = operators[%L]", operatorsListBuilder.size)

            // Operator definition
            operatorsListBuilder.addItem(
                CodeBlock.builder().apply {
                    addLine("%T(", operator::class)
                    withIndent {
                        add("attributes = ")
                        add(MapInitBuilder().apply {
                            operator.attributes.forEach { (key, value) ->
                                addItem(CodeBlock.builder().apply {
                                    add("%S to ", key)
                                    add(generateAttribute(value))
                                }.build())
                            }
                        }.generate())
                        add(
                            """,
                            |inputs = listOf(${operator.inputs.joinToString { "\"$it\"" }}),
                            |outputs = listOf(${operator.outputs.joinToString { "\"$it\"" }}),
                            |""".trimMargin()
                        )
                    }
                    add(")")
                }.build()
            )

            // Input tensors
            // TODO ONNXSequence, ONNXMap
            add("val inputs = ")
            ListInitBuilder().also { listBuilder ->
                operator.inputs.forEach {
                    listBuilder.addItem(CodeBlock.of(
                        "${nameMapping(it)}?.%M(%S)",
                        MemberName("io.kinference.data.tensors", "asTensor", isExtension = true),
                        it
                    ))
                }
                addLine(listBuilder.generate())
            }

            // Apply operator
            addLine("val result = operator.apply(preparedTensorsContext, inputs)")
            addLine("val resultMap = operator.outputs.zip(result.map { it?.data }).toMap()")

            // Extract outputs
            operator.outputs.forEach {
                addLine(
                    "${nameMapping(it)} = resultMap[%S] ?: error(%S)",
                    it,
                    "Required output '$it' not provided by '${operator.info.name}' operator"
                )
            }
        }
    }

    private fun generateAttribute(attribute: Attribute<Any>): CodeBlock =
        CodeBlock.builder().apply {
            addLine("%T.create(", Attribute::class)
            withIndent {
                add(
                    "%T(name = %S, refAttrName = %S, type = %T.%L, ",
                    AttributeProto::class,
                    attribute.name,
                    attribute.refAttrName,
                    AttributeProto.AttributeType::class,
                    attribute.type
                )
                add(generateAttributeValue(attribute))
                addLine(")")
            }
            add(")")
        }.build()

    private fun generateAttributeValue(attribute: Attribute<Any>): CodeBlock =
        CodeBlock.builder().apply {
            when (attribute.type) {
                AttributeProto.AttributeType.FLOAT -> add("f = %LF", attribute.value)
                AttributeProto.AttributeType.INT -> add("i = %L", attribute.value)
                AttributeProto.AttributeType.STRING -> add("s = %S", attribute.value)
                AttributeProto.AttributeType.TENSOR -> TODO("Attribute is not supported yet")
                AttributeProto.AttributeType.GRAPH -> error("Attribute is not supported")
                AttributeProto.AttributeType.SPARSE_TENSOR -> TODO("Attribute is not supported yet")
                AttributeProto.AttributeType.FLOATS -> {
                    val values = attribute.value as FloatArray
                    addNamed(
                        "floats = floatArrayOf(${values.indices.joinToString { "%float${it}:LF" }})",
                        values.withIndex().associate { Pair("float${it.index}", it.value) }
                    )
                }
                AttributeProto.AttributeType.INTS -> {
                    val values = attribute.value as LongArray
                    addNamed(
                        "ints = longArrayOf(${values.indices.joinToString { "%int${it}:L" }})",
                        values.withIndex().associate { Pair("int${it.index}", it.value) }
                    )
                }
                AttributeProto.AttributeType.STRINGS -> {
                    val values = attribute.value as List<*>
                    addNamed(
                        "strings = arrayListOf(${values.indices.joinToString { "%string${it}:S" }})",
                        values.withIndex().associate { Pair("string${it.index}", it.value) }
                    )
                }
                AttributeProto.AttributeType.TENSORS -> TODO("Attribute is not supported yet")
                AttributeProto.AttributeType.GRAPHS -> error("Attribute is not supported")
                AttributeProto.AttributeType.SPARSE_TENSORS -> TODO("Attribute is not supported yet")
                AttributeProto.AttributeType.UNDEFINED -> error("Cannot get attribute ${attribute.name} type")
                else -> error("???")
            }
        }.build()
}
