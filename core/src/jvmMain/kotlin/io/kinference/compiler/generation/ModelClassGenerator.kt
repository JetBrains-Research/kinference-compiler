package io.kinference.compiler.generation

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.kinference.compiler.api.GeneratedONNXModel
import io.kinference.compiler.generation.info.TensorInfo
import io.kinference.compiler.generation.initializers.InitializerGenerator
import io.kinference.compiler.generation.models.ClassGenerator
import io.kinference.compiler.generation.models.ListInitBuilder
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.operators.OperatorGenerator
import io.kinference.compiler.generation.utils.addLine
import io.kinference.compiler.generation.utils.exact
import io.kinference.compiler.generation.utils.withControlFlow
import io.kinference.data.tensors.Tensor
import io.kinference.graph.Graph
import io.kinference.ndarray.arrays.NDArray
import io.kinference.operators.Operator
import io.kinference.protobuf.resolveLocalDataType
import io.kinference.types.ValueTypeInfo
import java.io.File
import kotlin.time.ExperimentalTime

/* Generates source code for ONNX graph and puts initializers into resourceDirectory */
@OptIn(ExperimentalTime::class)
class ModelClassGenerator(
    private val graph: Graph,
    resourceDirectory: File,
    implementationClass: ClassName
) : ClassGenerator(implementationClass) {
    private val resourcePath = "onnx/initializers/${implementationClass.canonicalName}".replace(".", "/")
    private val initializersOutputDirectory = resourceDirectory.resolve(resourcePath)

    private val nameOrder: MutableMap<String, Int> = HashMap()
    private val nameMapping: (String) -> String = { name -> "tensors[${nameOrder[name]}]" }
    private val tensorInfo: MutableMap<String, TensorInfo> = HashMap()

    private val tensorLastUsageIndex: MutableMap<String, Int> = HashMap()
    private val tensorLastUsageIndexLambda: (String) -> Int = { name ->
        tensorLastUsageIndex.getOrDefault(name, Int.MAX_VALUE)
    }

    private val operatorsListBuilder = ListInitBuilder()

    init {
        initializersOutputDirectory.mkdirs()

        graph.initializers.forEachIndexed { index, tensor ->
            nameOrder[tensor.info.name] = index
            tensorInfo[tensor.info.name] = (tensor.info.typeInfo as ValueTypeInfo.TensorTypeInfo).let {
                TensorInfo(
                    shape = it.shape.getDimensions(),
                    dataType = it.type.resolveLocalDataType().exact(),
                    tensor = tensor
                )
            }
        }

        (graph.inputs + graph.outputs).forEach { valueInfo ->
            (valueInfo.typeInfo as? ValueTypeInfo.TensorTypeInfo)?.let { typeInfo ->
                tensorInfo[valueInfo.name] = TensorInfo(
                    shape = typeInfo.shape.getDimensions(),
                    dataType = typeInfo.type.resolveLocalDataType().exact()
                )
            }
        }

        graph.operators.forEachIndexed { operatorIndex, operator ->
            operator.inputs.forEach {
                tensorLastUsageIndex[it] = operatorIndex
            }

            (operator.inputs + operator.outputs).forEach {
                if (!nameOrder.contains(it)) {
                    nameOrder[it] = nameOrder.size
                }
            }
        }
        (graph.initializers.map { it.info.name } + graph.outputs.map { it.name }).forEach {
            tensorLastUsageIndex[it] = Int.MAX_VALUE
        }
    }

    override fun generateImpl() {
        builder.apply {
            addSuperinterface(GeneratedONNXModel::class)
            addAnnotation(
                AnnotationSpec.builder(ClassName("kotlin", "OptIn"))
                    .addMember("%T::class", ClassName("kotlin.time", "ExperimentalTime"))
                    .build()
            )
            addFunction(generatePredictFunction())
            addProperty(generateOperatorsProperty())
            addProperty(generateInitializersProperty())
        }
    }

    private fun generateInitializersProperty(): PropertySpec =
        PropertySpec.builder(
            "initializers",
            List::class.parameterizedBy(NDArray::class),
            KModifier.PRIVATE
        ).initializer(
            ListInitBuilder().apply {
                graph.initializers.forEach {
                    addItem(
                        InitializerGenerator(
                            initializersOutputDirectory,
                            resourcePath,
                            it
                        ).generate()
                    )
                }
            }.generate()
        ).build()

    private fun generateOperatorsProperty(): PropertySpec =
        PropertySpec.builder(
            "operators",
            List::class.asTypeName().parameterizedBy(
                Operator::class.asTypeName().parameterizedBy(
                    Tensor::class.asTypeName(), Tensor::class.asTypeName()
                )
            ),
            KModifier.PRIVATE
        ).initializer(operatorsListBuilder.generate()).build()

    private fun generatePredictFunction(): FunSpec {
        val ioMapType = Map::class.parameterizedBy(String::class, NDArray::class)
        return FunSpec.builder("predict").apply {
            addModifiers(KModifier.OVERRIDE)
            addParameter("inputTensors", ioMapType)
            returns(ioMapType)

            addAnnotation(AnnotationSpec.builder(Suppress::class).addMember("%S", "UNUSED_VARIABLE").build())

            addCode(generateInitialization())
            graph.operators.forEachIndexed { index, operator ->
                addCode(generateOperatorScope(operator, index))
            }
            addCode(generateReturn())
        }.build()
    }

    private fun generateInitialization(): CodeBlock =
        CodeBlock.builder().apply {
            addLine(
                "val tensors: Array<%T?> = Array(%L) { null }",
                NDArray::class,
                nameOrder.size
            )

            add(
                """
                |for (i in 0 until ${graph.initializers.size}) {
                |    tensors[i] = initializers[i]
                |}
                |""".trimMargin()
            )

            graph.availableInputs.forEach { input ->
                addLine(
                    "${nameMapping(input)} = inputTensors[%S] ?: error(%S)",
                    input,
                    "Input tensor '${input}' not provided"
                )
            }
        }.build()

    private fun generateOperatorScope(operator: Operator<*, *>, operatorIndex: Int): CodeBlock =
        CodeBlock.builder().apply {
            addLine("// ${operator.info.name}")
            withControlFlow("run") {
                add(OperatorGenerator(operator, info = OperatorGenerationInfo(
                    nameMapping, tensorLastUsageIndexLambda, tensorInfo, operatorsListBuilder, operatorIndex
                )).generate())
            }
        }.build()

    private fun generateReturn(): CodeBlock =
        CodeBlock.builder().apply {
            addLine("return mapOf(")
            withIndent {
                graph.outputs.forEach {
                    addLine("%S to ${nameMapping(it.name)}!!,", it.name)
                }
            }
            addLine(")")
        }.build()
}
