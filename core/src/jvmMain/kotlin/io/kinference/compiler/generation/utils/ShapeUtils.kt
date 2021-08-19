package io.kinference.compiler.generation.utils

import io.kinference.ndarray.arrays.tiled.IntTiledArray

fun IntArray.blockSize(): Int = IntTiledArray(shape = this).blockSize

fun IntArray.blocksNum(): Int = IntTiledArray(shape = this).blocksNum

fun IntArray.blocksInRow(): Int = last() / blockSize()

fun IntArray.actualAxis(axis: Int) = if (axis < 0) size + axis else axis
