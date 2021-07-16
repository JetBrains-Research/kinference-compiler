package io.kinference.compiler.plugin.generation.utils

import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl

fun IrBuilderWithScope.irInt(value: Int) = IrConstImpl.int(
    startOffset = UNDEFINED_OFFSET,
    endOffset = UNDEFINED_OFFSET,
    type = context.irBuiltIns.intType,
    value = value
)

fun IrBuilderWithScope.irFloat(value: Float) = IrConstImpl.float(
    startOffset = UNDEFINED_OFFSET,
    endOffset = UNDEFINED_OFFSET,
    type = context.irBuiltIns.floatType,
    value = value
)

fun IrBuilderWithScope.irString(value: String) = IrConstImpl.string(
    startOffset = UNDEFINED_OFFSET,
    endOffset = UNDEFINED_OFFSET,
    type = context.irBuiltIns.stringType,
    value = value
)
