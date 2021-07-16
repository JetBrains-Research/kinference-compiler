package io.kinference.compiler.plugin.generation.utils

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeWith

fun funArrayOf(pluginContext: IrPluginContext) =
    funWithVararg(pluginContext, "kotlin.arrayOf")

private fun funPrimitiveArrayOf(pluginContext: IrPluginContext, typeName: String) =
    funWithVararg(pluginContext, "kotlin.${typeName}ArrayOf")

fun funIntArrayOf(pluginContext: IrPluginContext) = funPrimitiveArrayOf(pluginContext, "int")
fun funFloatArrayOf(pluginContext: IrPluginContext) = funPrimitiveArrayOf(pluginContext, "float")

fun <T> IrBuilderWithScope.irPrimitiveArray(
    collectingFunction: IrSimpleFunctionSymbol,
    valueToIrConst: (T) -> IrExpression,
    elementType: IrType,
    elements: List<T>
): IrExpression {
    return irCall(collectingFunction).also { call ->
        call.putValueArgument(0, irVarargPrimitive(
            elementType = elementType,
            elements = elements.map { valueToIrConst(it) }
        ))
    }
}

fun IrBuilderWithScope.irArray(pluginContext: IrPluginContext, elementType: IrType, elements: List<IrExpression>) =
    irCall(funArrayOf(pluginContext), type = context.irBuiltIns.arrayClass.typeWith(elementType)).also { call ->
        call.putTypeArgument(0, elementType)
        call.putValueArgument(0, irVarargOut(elementType = elementType, elements = elements))
    }

fun IrBuilderWithScope.irIntArray(pluginContext: IrPluginContext, elements: List<Int>) =
    irPrimitiveArray(
        funIntArrayOf(pluginContext),
        ::irInt,
        pluginContext.irBuiltIns.intType,
        elements
    )

fun IrBuilderWithScope.irFloatArray(pluginContext: IrPluginContext, elements: List<Float>) =
    irPrimitiveArray(
        funFloatArrayOf(pluginContext),
        ::irFloat,
        pluginContext.irBuiltIns.floatType,
        elements
    )
