package io.kinference.compiler.serialization

import java.nio.ByteBuffer

private inline fun <reified T, reified V> ByteArray.toTiledArray(
    blockSize: Int,
    blocksNum: Int,
    constructor: (Int, (Int) -> V) -> T,
    crossinline getValue: ByteBuffer.() -> V
): Array<T> {
    val buffer = ByteBuffer.wrap(this)
    return Array(blocksNum) { constructor(blockSize) { buffer.getValue() } }
}

actual fun ByteArray.toShortTiledArray(blockSize: Int, blocksNum: Int): Array<ShortArray> =
    toTiledArray(blockSize, blocksNum, ::ShortArray, ByteBuffer::getShort)

actual fun ByteArray.toIntTiledArray(blockSize: Int, blocksNum: Int): Array<IntArray> =
    toTiledArray(blockSize, blocksNum, ::IntArray, ByteBuffer::getInt)

actual fun ByteArray.toLongTiledArray(blockSize: Int, blocksNum: Int): Array<LongArray> =
    toTiledArray(blockSize, blocksNum, ::LongArray, ByteBuffer::getLong)

actual fun ByteArray.toFloatTiledArray(blockSize: Int, blocksNum: Int): Array<FloatArray> =
    toTiledArray(blockSize, blocksNum, ::FloatArray, ByteBuffer::getFloat)

actual fun ByteArray.toDoubleTiledArray(blockSize: Int, blocksNum: Int): Array<DoubleArray> =
    toTiledArray(blockSize, blocksNum, ::DoubleArray, ByteBuffer::getDouble)

private inline fun <reified T> Array<T>.toByteArray(
    sizeBytes: Int,
    getSize: T.() -> Int,
    process: ByteBuffer.(T) -> Unit,
): ByteArray {
    val buffer = ByteBuffer.allocate(if (isEmpty()) 0 else size * first().getSize() * sizeBytes)
    forEach {
        buffer.process(it)
    }
    return buffer.array()
}

actual fun Array<ShortArray>.toByteArray(): ByteArray =
    toByteArray(Short.SIZE_BYTES, { size }) { it.forEach { value -> putShort(value) } }

actual fun Array<IntArray>.toByteArray(): ByteArray =
    toByteArray(Int.SIZE_BYTES, { size }) { it.forEach { value -> putInt(value) } }

actual fun Array<LongArray>.toByteArray(): ByteArray =
    toByteArray(Long.SIZE_BYTES, { size }) { it.forEach { value -> putLong(value) } }

actual fun Array<FloatArray>.toByteArray(): ByteArray =
    toByteArray(Float.SIZE_BYTES, { size }) { it.forEach { value -> putFloat(value) } }

actual fun Array<DoubleArray>.toByteArray(): ByteArray =
    toByteArray(Double.SIZE_BYTES, { size }) { it.forEach { value -> putDouble(value) } }
