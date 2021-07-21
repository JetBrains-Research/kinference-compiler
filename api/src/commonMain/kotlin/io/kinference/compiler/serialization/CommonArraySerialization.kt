package io.kinference.compiler.serialization

fun ByteArray.toByteTiledArray(blockSize: Int, blocksNum: Int): Array<ByteArray> =
    Array(blocksNum) { block -> ByteArray(blockSize) { i -> this[block * blockSize + i] } }

fun ByteArray.toBooleanTiledArray(blockSize: Int, blocksNum: Int): Array<BooleanArray> =
    Array(blocksNum) { block -> BooleanArray(blockSize) { i -> this[block * blockSize + i] != 0.toByte() } }

expect fun ByteArray.toShortTiledArray(blockSize: Int, blocksNum: Int): Array<ShortArray>
expect fun ByteArray.toIntTiledArray(blockSize: Int, blocksNum: Int): Array<IntArray>
expect fun ByteArray.toLongTiledArray(blockSize: Int, blocksNum: Int): Array<LongArray>
expect fun ByteArray.toFloatTiledArray(blockSize: Int, blocksNum: Int): Array<FloatArray>
expect fun ByteArray.toDoubleTiledArray(blockSize: Int, blocksNum: Int): Array<DoubleArray>

fun Array<ByteArray>.toByteArray(): ByteArray = flatMap { it.toList() }.toByteArray()

fun Array<BooleanArray>.toByteArray(): ByteArray =
    flatMap { it.toList() }.map { if (it) 1.toByte() else 0.toByte() }.toByteArray()

expect fun Array<ShortArray>.toByteArray(): ByteArray
expect fun Array<IntArray>.toByteArray(): ByteArray
expect fun Array<LongArray>.toByteArray(): ByteArray
expect fun Array<FloatArray>.toByteArray(): ByteArray
expect fun Array<DoubleArray>.toByteArray(): ByteArray
