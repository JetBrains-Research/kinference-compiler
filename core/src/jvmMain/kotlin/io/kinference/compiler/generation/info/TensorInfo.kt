package io.kinference.compiler.generation.info

import io.kinference.compiler.generation.utils.DataTypeInfo
import io.kinference.data.tensors.Tensor

/* Inferred info about tensor. */
data class TensorInfo(
    val shape: IntArray,
    val dataType: DataTypeInfo,
    val tensor: Tensor? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TensorInfo

        if (!shape.contentEquals(other.shape)) return false
        if (dataType != other.dataType) return false
        if (tensor != other.tensor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = shape.contentHashCode()
        result = 31 * result + dataType.hashCode()
        result = 31 * result + (tensor?.hashCode() ?: 0)
        return result
    }
}
