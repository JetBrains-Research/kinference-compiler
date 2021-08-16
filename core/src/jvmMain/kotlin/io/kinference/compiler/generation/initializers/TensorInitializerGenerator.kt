package io.kinference.compiler.generation.initializers

import io.kinference.compiler.generation.models.CodeBlockGenerator
import io.kinference.compiler.generation.utils.deserializerName
import io.kinference.compiler.generation.utils.exact
import io.kinference.compiler.generation.utils.ndArrayTypeName
import io.kinference.compiler.generation.utils.tiledArrayTypeName
import io.kinference.compiler.serialization.toByteArray
import io.kinference.data.tensors.Tensor
import io.kinference.ndarray.Strides
import io.kinference.ndarray.arrays.*
import io.kinference.ndarray.arrays.tiled.FloatTiledArray
import java.io.File

class TensorInitializerGenerator(
    private val outputDirectory: File,
    private val resourcePath: String,
    private val tensor: Tensor
) : CodeBlockGenerator() {
    override fun generateImpl() {
        val data = tensor.data
        val (serializedTensor, blockSize, blocksNum) = when (data) {
            is ByteNDArray -> Triple(data.array.blocks.toByteArray(), data.array.blockSize, data.array.blocksNum)
            is ShortNDArray -> Triple(data.array.blocks.toByteArray(), data.array.blockSize, data.array.blocksNum)
            is IntNDArray -> Triple(data.array.blocks.toByteArray(), data.array.blockSize, data.array.blocksNum)
            is LongNDArray -> Triple(data.array.blocks.toByteArray(), data.array.blockSize, data.array.blocksNum)
            is FloatNDArray -> Triple(data.array.blocks.toByteArray(), data.array.blockSize, data.array.blocksNum)
            is DoubleNDArray -> Triple(data.array.blocks.toByteArray(), data.array.blockSize, data.array.blocksNum)
            is BooleanNDArray -> Triple(data.array.blocks.toByteArray(), data.array.blockSize, data.array.blocksNum)
            else -> error("DataType '${data.type}' not supported")
        }
        FloatTiledArray

        val initializerFileName = "${tensor.info.name}.bin"
        val initializerFile = outputDirectory.resolve(initializerFileName)
        initializerFile.writeBytes(serializedTensor)

        val dataType = tensor.data.type.exact()
        builder.add(
            """
            |%T(
            |    array = %T(
            |        this::class.java.getResource(
            |            "/${resourcePath}/${initializerFileName}"
            |        )!!.readBytes().%M(
            |            blockSize = $blockSize, blocksNum = $blocksNum
            |        )
            |    ),
            |    strides = %T(shape = intArrayOf(${data.shape.joinToString()}))
            |)""".trimMargin(),
            dataType.ndArrayTypeName(),
            dataType.tiledArrayTypeName(),
            dataType.deserializerName(),
            Strides::class
        )
    }
}
