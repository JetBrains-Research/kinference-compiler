package io.kinference.compiler.generation.utils

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import io.kinference.primitives.types.DataType

data class DataTypeInfo(
    val dataType: DataType,
    val nullable: Boolean = false
)
fun DataType.exact() = DataTypeInfo(this)
fun DataType.nullable() = DataTypeInfo(this, nullable = true)

fun DataTypeInfo.questionMark() = if (nullable) "?" else ""

fun DataTypeInfo.dataTypeName() = when (dataType) {
    DataType.ALL -> ""
    else -> dataType.toString().toLowerCase().capitalize()
}

fun DataTypeInfo.ndArrayTypeName() = ClassName(
    "io.kinference.ndarray.arrays",
    "${dataTypeName()}NDArray"
)

fun DataTypeInfo.mutableNDArrayTypeName() = ClassName(
    "io.kinference.ndarray.arrays",
    "Mutable${dataTypeName()}NDArray"
)

fun DataTypeInfo.tiledArrayTypeName() = ClassName(
    "io.kinference.ndarray.arrays.tiled",
    "${dataTypeName()}TiledArray"
)

fun DataTypeInfo.deserializerName() = MemberName(
    "io.kinference.compiler.serialization",
    "to${dataTypeName()}TiledArray",
    isExtension = true
)
