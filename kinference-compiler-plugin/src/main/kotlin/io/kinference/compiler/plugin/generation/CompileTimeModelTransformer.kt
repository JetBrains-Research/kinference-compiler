package io.kinference.compiler.plugin.generation

import io.kinference.attributes.Attribute
import io.kinference.compiler.CompileTimeModel
import io.kinference.compiler.CompileTimeModelImpl
import io.kinference.compiler.plugin.generation.utils.*
import io.kinference.compiler.plugin.utils.log
import io.kinference.data.ONNXData
import io.kinference.data.tensors.Tensor
import io.kinference.graph.Graph
import io.kinference.model.Model
import io.kinference.ndarray.Strides
import io.kinference.ndarray.arrays.FloatNDArray
import io.kinference.ndarray.arrays.tiled.FloatTiledArray
import io.kinference.onnx.TensorProto
import io.kinference.operators.Operator
import io.kinference.types.TensorShape
import io.kinference.types.ValueInfo
import io.kinference.types.ValueTypeInfo
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.js.utils.asString
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrGetEnumValueImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.name.FqName

@ObsoleteDescriptorBasedAPI
class CompileTimeModelTransformer(
    private val pluginContext: IrPluginContext,
    private val messageCollector: MessageCollector? = null,
) : IrElementTransformerVoidWithContext() {
    override fun visitConstructorCall(expression: IrConstructorCall): IrExpression {
        if (expression.type.asString() == "io.kinference.compiler.CompileTimeModel" && expression.valueArgumentsCount == 1) {
            val fileNameArgument = expression.getValueArgument(0)!!
            if (fileNameArgument !is IrConst<*>) {
                error("fileName must be compile-time constant")
            }
            val fileName = fileNameArgument.value.toString()
            messageCollector?.log("Transforming CompileTimeModel(\"$fileName\")")

            val model = Model.load(fileName)
            val graph = model.graph

            val builder = newIrBuilder()

            val operators = builder.generateOperators(graph.operators)
            val outputs = builder.generateOutputs(graph.outputs)
            val initializers = builder.generateInitializers(graph.initializers)
            val valueOrderInfo = builder.generateValueOrderInfo(getValueOrderInfo(graph))
            val availableInputs = builder.generateAvailableInputs(graph.availableInputs)

            val compiledModel = builder.irConstructorCall<CompileTimeModelImpl>(
                pluginContext,
                listOf("operators", "outputs", "initializers", "valueOrderInfo", "availableInputs"),
                listOf(operators, outputs, initializers, valueOrderInfo, availableInputs)
            )
            val result = irTypeCast(findIrClass<CompileTimeModel>(pluginContext)!!.defaultType, compiledModel)
            messageCollector?.log("RESULT:\n${result.dump()}")
            messageCollector?.log("Successfully transformed CompileTimeModel(\"$fileName\")")

            return result
        }
        return super.visitConstructorCall(expression)
    }

    private fun IrBuilderWithScope.generateOperators(operators: List<Operator<ONNXData, ONNXData>>): IrExpression {
        val operatorType = findIrClass<Operator<*, *>>(pluginContext)!!.typeWith(
            List(2) { findIrClass<ONNXData>(pluginContext)!!.defaultType }
        )
        return irList(
            pluginContext,
            operatorType,
            operators.map {
                irTypeCast(
                    type = operatorType,
                    argument = generateOperator (it)
                )
            }
        )
    }

    private fun IrBuilderWithScope.generateOutputs(outputs: List<ValueInfo>): IrExpression =
        irList(
            pluginContext,
            findIrClass<ValueInfo>(pluginContext)!!.defaultType,
            outputs.map { generateValueInfo(it) }
        )

    private fun IrBuilderWithScope.generateInitializers(initializers: List<Tensor>): IrExpression =
        irList(
            pluginContext,
            findIrClass<Tensor>(pluginContext)!!.defaultType,
            initializers.map { generateTensor(it) }
        )

    private fun IrBuilderWithScope.generateValueOrderInfo(valueOrderInfo: Map<String, Int>): IrExpression =
        irMap(
            pluginContext,
            findIrClass<String>(pluginContext)!!.defaultType to findIrClass<Int>(pluginContext)!!.defaultType,
            valueOrderInfo.entries.map { (key, value) ->
                irPair(
                    pluginContext,
                    typeArguments = context.irBuiltIns.stringType to context.irBuiltIns.intType,
                    irString(key) to irInt(value)
                )
            }
        )

    private fun IrBuilderWithScope.generateAvailableInputs(availableInputs: List<String>): IrExpression =
        irList(
            pluginContext,
            context.irBuiltIns.stringType,
            availableInputs.map { irString(it) }
        )

    private fun IrBuilderWithScope.generateOperator(operator: Operator<ONNXData, ONNXData>): IrExpression {
        val attributes = irMap(
            pluginContext,
            context.irBuiltIns.stringType to findIrClass<Attribute<*>>(pluginContext)!!.typeWith(context.irBuiltIns.anyType),
            emptyList() // FIXME
        )
        val inputs = irList(
            pluginContext,
            context.irBuiltIns.stringType,
            operator.inputs.map { irString(it) }
        )
        val outputs = irList(
            pluginContext,
            context.irBuiltIns.stringType,
            operator.outputs.map { irString(it) }
        )

        return irConstructorCall(
            pluginContext,
            operator::class.qualifiedName!!,
            listOf("attributes", "inputs", "outputs"),
            listOf(attributes, inputs, outputs)
        )
    }

    private fun IrBuilderWithScope.generateFloatTiledArray(data: FloatTiledArray): IrExpression =
        irConstructorCall<FloatTiledArray>(
            pluginContext,
            listOf("blocks"),
            listOf(irArray(
                pluginContext,
                elementType = context.irBuiltIns.primitiveArrayForType[context.irBuiltIns.floatType]!!.defaultType,
                elements = data.blocks.map { irFloatArray(pluginContext, it.toList()) }
            ))
        )

    private fun IrBuilderWithScope.generateStrides(strides: Strides): IrExpression =
        irConstructorCall<Strides>(
            pluginContext,
            listOf("shape"),
            listOf(irIntArray(pluginContext, elements = strides.shape.toList()))
        )

    private fun IrBuilderWithScope.generateFloatNDArray(data: FloatNDArray): IrExpression =
        irConstructorCall<FloatNDArray>(
            pluginContext,
            listOf("array", "strides"),
            listOf(generateFloatTiledArray(data.array), generateStrides(data.strides))
        )

    private fun IrBuilderWithScope.generateValueInfo(info: ValueInfo): IrExpression {
        if (info.typeInfo !is ValueTypeInfo.TensorTypeInfo) {
            error("Not yet implemented")
        }
        val typeInfo = irConstructorCall<ValueTypeInfo.TensorTypeInfo>(
            pluginContext,
            listOf("shape", "type"),
            listOf(
                irConstructorCall<TensorShape>(
                    pluginContext,
                    listOf("shape"),
                    listOf(irIntArray(pluginContext, elements = (info.typeInfo as ValueTypeInfo.TensorTypeInfo).shape.getDimensions().toList()))
                ),
                IrGetEnumValueImpl(
                    startOffset = UNDEFINED_OFFSET,
                    endOffset = UNDEFINED_OFFSET,
                    type = findIrClass<TensorProto.DataType>(pluginContext)!!.defaultType,
                    symbol = pluginContext.symbolTable.referenceEnumEntry(
                        pluginContext.referenceClass(FqName("${TensorProto.DataType::class.qualifiedName}.FLOAT"))!!.descriptor
                    )
                )
            )
        )
        val name = irString(info.name)
        return irConstructorCall<ValueInfo>(
            pluginContext,
            listOf("typeInfo", "name"),
            listOf(typeInfo, name)
        )
    }

    private fun IrBuilderWithScope.generateTensor(tensor: Tensor): IrExpression =
        irConstructorCall<Tensor>(
            pluginContext,
            listOf("data", "info"),
            listOf(generateFloatNDArray(tensor.data as FloatNDArray), generateValueInfo(tensor.info)) // FIXME
        )

    private fun getValueOrderInfo(graph: Graph): Map<String, Int> {
        val valueOrderInfo = HashMap<String, Int>()
        for ((i, operator) in graph.operators.withIndex()) {
            for (input in operator.inputs) {
                valueOrderInfo[input] = i
            }
        }
        val toRemove = (graph.initializers.map { it.info.name } + graph.outputs.map { it.name }).toSet()
        return valueOrderInfo.filterKeys { it !in toRemove }
    }

    private fun newIrBuilder() =
        object : IrBuilderWithScope(pluginContext, currentScope!!.scope, UNDEFINED_OFFSET, UNDEFINED_OFFSET) {}
}
