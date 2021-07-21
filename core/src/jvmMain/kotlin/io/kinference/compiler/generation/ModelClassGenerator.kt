package io.kinference.compiler.generation

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.kinference.compiler.generation.initializers.InitializerGenerator
import io.kinference.compiler.generation.models.ClassGenerator
import io.kinference.compiler.generation.operators.OperatorGenerator
import io.kinference.graph.Graph
import io.kinference.ndarray.arrays.IntNDArray
import io.kinference.ndarray.arrays.NDArray
import io.kinference.operators.Operator
import java.io.File

class ModelClassGenerator(
    private val graph: Graph,
    resourceDirectory: File,
    implementationClass: ClassName
) : ClassGenerator(implementationClass) {
    private val resourcePath = "onnx/initializers/${implementationClass.canonicalName}".replace(".", "/")
    private val initializersOutputDirectory = resourceDirectory.resolve(resourcePath)

    private val initializers = graph.initializers.associateBy { it.info.name }

    private val tensorRemoveOrder: List<MutableList<String>>
    private val tensorInitOrder: List<MutableList<String>>
    private val nameOrder: MutableMap<String, Int> = HashMap()
    private val nameMapping: (String) -> String = { name -> "tensors[${nameOrder[name]}]" }

    init {
        initializersOutputDirectory.mkdirs()

        val removeOrderMap = HashMap<String, Int>()
        val initOrderMap = HashMap<String, Int>()

        graph.operators.forEachIndexed { index, operator ->
            operator.inputs.forEach { input ->
                if (!nameOrder.contains(input)) {
                    nameOrder[input] = nameOrder.size
                }

                if (initializers.contains(input) && !initOrderMap.contains(input)) {
                    initOrderMap[input] = index
                }

                removeOrderMap[input] = index
            }

            operator.outputs.forEach { output ->
                if (!nameOrder.contains(output)) {
                    nameOrder[output] = nameOrder.size
                }
            }
        }

        tensorInitOrder = List(graph.operators.size) { ArrayList() }
        initOrderMap.forEach { (name, index) ->
            tensorInitOrder[index].add(name)
        }

        graph.outputs.forEach {
            removeOrderMap.remove(it.name)
        }
        tensorRemoveOrder = List(graph.operators.size) { ArrayList() }
        removeOrderMap.forEach { (name, index) ->
            tensorRemoveOrder[index].add(name)
        }
    }

    override fun generateImpl() {
        builder.apply {
            addFunction(generatePredictFunction())
        }
    }

    private fun generatePredictFunction(): FunSpec {
        val ioMapType = Map::class.parameterizedBy(String::class, NDArray::class)
        return FunSpec.builder("predict").apply {
            addParameter("inputTensors", ioMapType)
            returns(ioMapType)

            addCode(generateInitialization())
            graph.operators.forEachIndexed { index, operator ->
                addCode(generateOperatorScope(operator, index))
            }
            addCode(generateReturn())
        }.build()
    }

    private fun generateInitialization(): CodeBlock =
        CodeBlock.builder().apply {
            addStatement(
                "val emptyNDArray: %T = %T(shape = intArrayOf(0))",
                NDArray::class,
                IntNDArray::class
            )
            addStatement(
                "val tensors: Array<%T> = Array(${nameOrder.size}) { emptyNDArray }",
                NDArray::class
            )

            graph.availableInputs.forEach { input ->
                addStatement(
                    "${nameMapping(input)} = inputTensors[%S] ?: error(%S)",
                    input,
                    "Input tensor '${input}' not provided"
                )
            }
        }.build()

    private fun generateOperatorScope(operator: Operator<*, *>, index: Int): CodeBlock =
        CodeBlock.builder().apply {
            beginControlFlow("run")

            tensorInitOrder[index].forEach { name ->
                add(InitializerGenerator(
                    initializersOutputDirectory,
                    resourcePath,
                    initializers[name]!!,
                    nameMapping
                ).generate())
            }
            add(OperatorGenerator(operator, nameMapping).generate())

            add("// For garbage collector\n")
            tensorRemoveOrder[index].forEach { name ->
                addStatement("${nameMapping(name)} = emptyNDArray")
            }

            endControlFlow()
        }.build()

    private fun generateReturn(): CodeBlock =
        CodeBlock.builder().apply {
            add("return mapOf(\n")
            indent()
            graph.outputs.forEach {
                add("%S to ${nameMapping(it.name)},\n", it.name)
            }
            unindent()
            add(")\n")
        }.build()
}
