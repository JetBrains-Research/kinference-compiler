package io.kinference.compiler.plugin.generation.utils

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.impl.IrTypeOperatorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.types.typeWithArguments
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.types.Variance

fun IrBuilderWithScope.irVarargPrimitive(elementType: IrType, elements: List<IrExpression>) =
    IrVarargImpl(
        startOffset = UNDEFINED_OFFSET,
        endOffset = UNDEFINED_OFFSET,
        type = context.irBuiltIns.primitiveArrayForType[elementType]!!.defaultType,
        varargElementType = elementType,
        elements = elements
    )

fun IrBuilderWithScope.irVarargOut(elementType: IrType, elements: List<IrExpression>) =
    IrVarargImpl(
        startOffset = UNDEFINED_OFFSET,
        endOffset = UNDEFINED_OFFSET,
        type = context.irBuiltIns.arrayClass.typeWithArguments(listOf(makeTypeProjection(elementType, Variance.OUT_VARIANCE))),
        varargElementType = elementType,
        elements = elements
    )

fun irTypeCast(type: IrType, argument: IrExpression) =
    IrTypeOperatorCallImpl(
        startOffset = UNDEFINED_OFFSET,
        endOffset = UNDEFINED_OFFSET,
        type = type,
        operator = IrTypeOperator.CAST,
        typeOperand = type,
        argument = argument
    )

inline fun <reified T> findIrClass(pluginContext: IrPluginContext) =
    T::class.qualifiedName?.let { pluginContext.referenceClass(FqName(it)) }

inline fun findConstructor(pluginContext: IrPluginContext, fqName: String, filter: (IrConstructorSymbol) -> Boolean) =
    pluginContext.referenceConstructors(FqName(fqName)).single(filter)

inline fun <reified T> findConstructor(pluginContext: IrPluginContext, filter: (IrConstructorSymbol) -> Boolean) =
    findConstructor(pluginContext, T::class.qualifiedName!!, filter)

fun findConstructor(pluginContext: IrPluginContext, fqName: String, parameterNames: List<String>) =
    findConstructor(pluginContext, fqName) {
        it.owner.valueParameters.map { parameter -> parameter.name.toString() } == parameterNames
    }

inline fun <reified T> findConstructor(pluginContext: IrPluginContext, parameterNames: List<String>) =
    findConstructor<T>(pluginContext) {
        it.owner.valueParameters.map { parameter -> parameter.name.toString() } == parameterNames
    }

fun funWithVararg(pluginContext: IrPluginContext, functionName: String): IrSimpleFunctionSymbol =
    pluginContext.referenceFunctions(FqName(functionName))
        .toSet() // FIXME I'm not sure it's ok that we need this
        .single {
            val parameters = it.owner.valueParameters
            parameters.size == 1 && parameters[0].isVararg
        }

inline fun <reified T> IrBuilderWithScope.irConstructorCall(
    pluginContext: IrPluginContext,
    parameterNames: List<String>,
    parameters: List<IrExpression>,
    typeArguments: List<IrType> = emptyList()
) = irCall(findConstructor<T>(pluginContext, parameterNames)).also { call ->
        typeArguments.withIndex().forEach { (i, typeArgument) ->
            call.putTypeArgument(i, typeArgument)
        }
        parameters.withIndex().forEach { (i, parameter) ->
            call.putValueArgument(i, parameter)
        }
    }

fun IrBuilderWithScope.irConstructorCall(
    pluginContext: IrPluginContext,
    fqName: String,
    parameterNames: List<String>,
    parameters: List<IrExpression>
) = irCall(findConstructor(pluginContext, fqName, parameterNames)).also { call ->
    parameters.withIndex().forEach { (i, parameter) ->
        call.putValueArgument(i, parameter)
    }
}

fun IrBuilderWithScope.irPair(
    pluginContext: IrPluginContext,
    typeArguments: Pair<IrType, IrType>,
    elements: Pair<IrExpression, IrExpression>
) = irConstructorCall<Pair<*, *>>(
    pluginContext,
    parameterNames = listOf("first", "second"),
    parameters = elements.toList(),
    typeArguments = typeArguments.toList()
)
