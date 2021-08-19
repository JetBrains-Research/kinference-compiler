package io.kinference.compiler.generation.utils

import io.kinference.ndarray.arrays.tiled.PrimitiveTiledArray

fun IntArray.hasOneBlockInRow() = last() < PrimitiveTiledArray.MIN_BLOCK_SIZE

fun IntArray.actualAxis(axis: Int) = if (axis < 0) size + axis else axis
