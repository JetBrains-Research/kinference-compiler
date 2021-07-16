package io.kinference.compiler.plugin.generation.utils

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeWith

private fun funCollectionOf(pluginContext: IrPluginContext, collectionName: String) =
    funWithVararg(pluginContext, "kotlin.collections.${collectionName}Of")

fun funListOf(pluginContext: IrPluginContext) = funCollectionOf(pluginContext, "list")
fun funMapOf(pluginContext: IrPluginContext) = funCollectionOf(pluginContext, "map")

private fun IrBuilderWithScope.irCollection(
    collectingFunction: IrSimpleFunctionSymbol,
    collectionClass: IrClassSymbol,
    typeArguments: List<IrType>,
    elementType: IrType,
    elements: List<IrExpression>
) = irCall(collectingFunction, type = collectionClass.typeWith(typeArguments)).also { call ->
        typeArguments.withIndex().forEach { (i, typeArgument) ->
            call.putTypeArgument(i, typeArgument)
        }
        call.putValueArgument(0, irVarargOut(elementType = elementType, elements = elements))
    }

fun IrBuilderWithScope.irList(
    pluginContext: IrPluginContext,
    elementType: IrType,
    elements: List<IrExpression>
) = irCollection(
    collectingFunction = funListOf(pluginContext),
    collectionClass = findIrClass<List<*>>(pluginContext)!!,
    typeArguments = listOf(elementType),
    elementType = elementType,
    elements = elements
)

fun IrBuilderWithScope.irMap(
    pluginContext: IrPluginContext,
    typeArguments: Pair<IrType, IrType>,
    elements: List<IrExpression>
) = irCollection(
    collectingFunction = funMapOf(pluginContext),
    collectionClass = findIrClass<Map<*, *>>(pluginContext)!!,
    typeArguments = typeArguments.toList(),
    elementType = findIrClass<Pair<*, *>>(pluginContext)!!.typeWith(typeArguments.toList()),
    elements = elements
)
