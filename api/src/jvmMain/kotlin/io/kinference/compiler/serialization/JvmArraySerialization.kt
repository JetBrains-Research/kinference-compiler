package io.kinference.compiler.serialization

import java.nio.ByteBuffer

actual fun ByteArray.toShortTiledArray(blockSize: Int, blocksNum: Int): Array<ShortArray> {
    val buffer = ByteBuffer.wrap(this).asShortBuffer()
    return Array(blocksNum) { ShortArray(blockSize) { buffer.get() } }
}

actual fun ByteArray.toIntTiledArray(blockSize: Int, blocksNum: Int): Array<IntArray> {
    val buffer = ByteBuffer.wrap(this).asIntBuffer()
    return Array(blocksNum) { IntArray(blockSize) { buffer.get() } }
}

actual fun ByteArray.toLongTiledArray(blockSize: Int, blocksNum: Int): Array<LongArray> {
    val buffer = ByteBuffer.wrap(this).asLongBuffer()
    return Array(blocksNum) { LongArray(blockSize) { buffer.get() } }
}

actual fun ByteArray.toFloatTiledArray(blockSize: Int, blocksNum: Int): Array<FloatArray> {
    val buffer = ByteBuffer.wrap(this).asFloatBuffer()
    return Array(blocksNum) { FloatArray(blockSize) { buffer.get() } }
}

actual fun ByteArray.toDoubleTiledArray(blockSize: Int, blocksNum: Int): Array<DoubleArray> {
    val buffer = ByteBuffer.wrap(this).asDoubleBuffer()
    return Array(blocksNum) { DoubleArray(blockSize) { buffer.get() } }
}

actual fun Array<ShortArray>.toByteArray(): ByteArray {
    val buffer = ByteBuffer.allocate(if (isEmpty()) 0 else size * first().size * Short.SIZE_BYTES)
    forEach {
        it.forEach { value -> buffer.putShort(value) }
    }
    return buffer.array()
}

actual fun Array<IntArray>.toByteArray(): ByteArray {
    val buffer = ByteBuffer.allocate(if (isEmpty()) 0 else size * first().size * Int.SIZE_BYTES)
    forEach {
        it.forEach { value -> buffer.putInt(value) }
    }
    return buffer.array()
}

actual fun Array<LongArray>.toByteArray(): ByteArray {
    val buffer = ByteBuffer.allocate(if (isEmpty()) 0 else size * first().size * Long.SIZE_BYTES)
    forEach {
        it.forEach { value -> buffer.putLong(value) }
    }
    return buffer.array()
}

actual fun Array<FloatArray>.toByteArray(): ByteArray {
    val buffer = ByteBuffer.allocate(if (isEmpty()) 0 else size * first().size * Float.SIZE_BYTES)
    forEach {
        it.forEach { value -> buffer.putFloat(value) }
    }
    return buffer.array()
}

actual fun Array<DoubleArray>.toByteArray(): ByteArray {
    val buffer = ByteBuffer.allocate(if (isEmpty()) 0 else size * first().size * Double.SIZE_BYTES)
    forEach {
        it.forEach { value -> buffer.putDouble(value) }
    }
    return buffer.array()
}
