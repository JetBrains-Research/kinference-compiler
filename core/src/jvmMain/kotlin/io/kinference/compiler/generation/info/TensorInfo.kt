package io.kinference.compiler.generation.info

import io.kinference.compiler.generation.utils.DataTypeInfo
import io.kinference.data.tensors.Tensor

/* Inferred info about tensor. */
class TensorInfo(
    val shape: IntArray,
    val dataType: DataTypeInfo,
    val tensor: Tensor? = null
) {
    operator fun component1(): IntArray = shape
    operator fun component2(): DataTypeInfo = dataType
    operator fun component3(): Tensor? = tensor
}
