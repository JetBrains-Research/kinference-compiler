package io.kinference.compiler.serialization

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.DataView

private inline fun <reified T, reified V> ByteArray.toTiledArray(
    sizeBytes: Int,
    blockSize: Int,
    blocksNum: Int,
    constructor: (Int, (Int) -> V) -> T,
    crossinline getValue: DataView.(byteOffset: Int, littleEndian: Boolean) -> V
): Array<T> {
    val buffer = DataView(ArrayBuffer(size))
    forEachIndexed { index, byte ->
        buffer.setUint8(index, byte)
    }
    return Array(blocksNum) { block ->
        constructor(blockSize) { index ->
            buffer.getValue((block * blockSize + index) * sizeBytes, false)
        }
    }
}

actual fun ByteArray.toShortTiledArray(blockSize: Int, blocksNum: Int): Array<ShortArray> =
    toTiledArray(Short.SIZE_BYTES, blockSize, blocksNum, ::ShortArray, DataView::getInt16)

actual fun ByteArray.toIntTiledArray(blockSize: Int, blocksNum: Int): Array<IntArray> =
    toTiledArray(Int.SIZE_BYTES, blockSize, blocksNum, ::IntArray, DataView::getInt32)

actual fun ByteArray.toLongTiledArray(blockSize: Int, blocksNum: Int): Array<LongArray> =
    toTiledArray(Long.SIZE_BYTES, blockSize, blocksNum, ::LongArray) { byteOffset, littleEndian ->
        (getInt32(byteOffset, littleEndian).toLong() shl Int.SIZE_BITS) +
                getUint32(byteOffset + Int.SIZE_BYTES)
    }

actual fun ByteArray.toFloatTiledArray(blockSize: Int, blocksNum: Int): Array<FloatArray> =
    toTiledArray(Float.SIZE_BYTES, blockSize, blocksNum, ::FloatArray, DataView::getFloat32)

actual fun ByteArray.toDoubleTiledArray(blockSize: Int, blocksNum: Int): Array<DoubleArray> =
    toTiledArray(Double.SIZE_BYTES, blockSize, blocksNum, ::DoubleArray, DataView::getFloat64)

private inline fun <reified T, reified V> Array<T>.toByteArray(
    sizeBytes: Int,
    crossinline getSize: T.() -> Int,
    forEachIndexed: T.(action: (Int, V) -> Unit) -> Unit,
    crossinline setValue: DataView.(byteOffset: Int, value: V) -> Unit,
): ByteArray {
    val buffer = DataView(ArrayBuffer(if (isEmpty()) 0 else size * first().getSize() * sizeBytes))
    forEachIndexed { index, innerArray ->
        innerArray.forEachIndexed { innerIndex, value ->
            buffer.setValue((index * innerArray.getSize() + innerIndex) * sizeBytes, value)
        }
    }
    return ByteArray(buffer.byteLength) { index -> buffer.getUint8(index) }
}

actual fun Array<ShortArray>.toByteArray(): ByteArray =
    toByteArray(Short.SIZE_BYTES, { size }, ShortArray::forEachIndexed, DataView::setInt16)

actual fun Array<IntArray>.toByteArray(): ByteArray =
    toByteArray(Int.SIZE_BYTES, { size }, IntArray::forEachIndexed, DataView::setInt32)

actual fun Array<LongArray>.toByteArray(): ByteArray =
    toByteArray(Long.SIZE_BYTES, { size }, LongArray::forEachIndexed) { byteOffset, value ->
        setInt32(byteOffset, (value shr Int.SIZE_BITS).toInt())
        setUint32(byteOffset + Int.SIZE_BYTES, value.toInt())
    }

actual fun Array<FloatArray>.toByteArray(): ByteArray =
    toByteArray(Float.SIZE_BYTES, { size }, FloatArray::forEachIndexed, DataView::setFloat32)

actual fun Array<DoubleArray>.toByteArray(): ByteArray =
    toByteArray(Double.SIZE_BYTES, { size }, DoubleArray::forEachIndexed, DataView::setFloat64)
