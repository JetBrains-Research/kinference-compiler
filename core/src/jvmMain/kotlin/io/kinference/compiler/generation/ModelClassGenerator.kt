package io.kinference.compiler.generation

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.kinference.compiler.api.GeneratedONNXModel
import io.kinference.compiler.api.GeneratedONNXModelProfiler
import io.kinference.compiler.generation.info.TensorInfo
import io.kinference.compiler.generation.initializers.InitializerGenerator
import io.kinference.compiler.generation.models.ClassGenerator
import io.kinference.compiler.generation.context.ContextBuilder
import io.kinference.compiler.generation.models.ListInitBuilder
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.operators.OperatorGenerator
import io.kinference.compiler.generation.utils.addLine
import io.kinference.compiler.generation.utils.exact
import io.kinference.compiler.generation.utils.withControlFlow
import io.kinference.data.tensors.Tensor
import io.kinference.graph.Context
import io.kinference.graph.Graph
import io.kinference.graph.ProfileAnalysisEntry
import io.kinference.graph.ProfilingContext
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
    private val implementationClass: ClassName,
    private val implementProfiling: Boolean
) : ClassGenerator(implementationClass) {
    private val initializersPath = "onnx/initializers/".modelResourcePath()
    private val preparedTensorsPath = "onnx/prepared/".modelResourcePath()

    private val initializersOutputDirectory = resourceDirectory.resolve(initializersPath)
    private val preparedTensorsOutputDirectory = resourceDirectory.resolve(preparedTensorsPath)

    private val nameOrder: MutableMap<String, Int> = HashMap()
    private val nameMapping: (String) -> String = { name -> "tensors[${nameOrder[name]}]" }
    private val tensorInfo: MutableMap<String, TensorInfo> = HashMap()

    private val tensorLastUsageIndex: MutableMap<String, Int> = HashMap()
    private val tensorLastUsageIndexLambda: (String) -> Int = { name ->
        tensorLastUsageIndex.getOrDefault(name, Int.MAX_VALUE)
    }

    private val operatorsListBuilder = ListInitBuilder()
    private val preparedContextBuilder = ContextBuilder(preparedTensorsOutputDirectory, preparedTensorsPath)

    init {
        initializersOutputDirectory.mkdirs()
        preparedTensorsOutputDirectory.mkdirs()

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
        (graph.initializers.map { it.info.name } + graph.availableInputs + graph.outputs.map { it.name }).forEach {
            tensorLastUsageIndex[it] = Int.MAX_VALUE
        }
    }

    private fun String.modelResourcePath() =
        "$this${implementationClass.canonicalName}".replace(".", "/")

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
            addProperty(generatePreparedTensorsContextProperty())

            if (implementProfiling) {
                addSuperinterface(GeneratedONNXModelProfiler::class)
                addProperty(generateProfilesProperty())
                addFunction(generateAnalyzeProfilingResultsFunction())
                addFunction(generateResetProfilesFunction())
            }
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
                            initializersPath,
                            it
                        ).generate()
                    )
                }
            }.generate()
        ).build()

    private fun generatePreparedTensorsContextProperty(): PropertySpec =
        PropertySpec.builder(
            "preparedTensorsContext",
            Context::class,
            KModifier.PRIVATE
        ).initializer(
            preparedContextBuilder.generate()
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

    private fun generatePredictFunction(): FunSpec =
        FunSpec.builder("predict").apply {
            val ioMapType = Map::class.parameterizedBy(String::class, NDArray::class)

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

    private fun generateInitialization(): CodeBlock =
        CodeBlock.builder().apply {
            if (implementProfiling) {
                add(
                    """
                    |val profilingContext = %T(%S).apply {
                    |    profiles.add(this)
                    |}
                    |""".trimMargin(),
                    ProfilingContext::class,
                    "Generated model ${implementationClass.simpleName}"
                )
            }

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
            addLine("/* ${operator.info.name} */")
            withControlFlow("run") {
                withControlFlow(implementProfiling, "profilingContext.profile(%S)", operator.info.name) {
                    add(OperatorGenerator(operator, info = OperatorGenerationInfo(
                        nameMapping,
                        tensorLastUsageIndexLambda,
                        tensorInfo,
                        operatorsListBuilder,
                        preparedContextBuilder,
                        operatorIndex
                    )).generate())
                }
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

    private fun generateProfilesProperty(): PropertySpec =
        PropertySpec.builder(
            "profiles",
            ArrayList::class.asTypeName().parameterizedBy(ProfilingContext::class.asTypeName()),
            KModifier.PRIVATE
        ).initializer("ArrayList()").build()

    private fun generateAnalyzeProfilingResultsFunction(): FunSpec =
        FunSpec.builder("analyzeProfilingResults").apply {
            addModifiers(KModifier.OVERRIDE)
            returns(ProfileAnalysisEntry::class)
            addCode(
                "return profiles.%M(%S)",
                MemberName("io.kinference.graph", "analyze"),
                "Generated model ${implementationClass.simpleName}"
            )
        }.build()

    private fun generateResetProfilesFunction(): FunSpec =
        FunSpec.builder("resetProfiles").apply {
            addModifiers(KModifier.OVERRIDE)
            addCode("return profiles.clear()")
        }.build()
}
