package org.example

import io.kinference.`data`.tensors.asTensor
import io.kinference.compiler.serialization.toFloatTiledArray
import io.kinference.graph.Context
import io.kinference.ndarray.Strides
import io.kinference.ndarray.arrays.FloatNDArray
import io.kinference.ndarray.arrays.IntNDArray
import io.kinference.ndarray.arrays.NDArray
import io.kinference.ndarray.arrays.NumberNDArray
import io.kinference.ndarray.arrays.tiled.FloatTiledArray
import io.kinference.operators.math.MatMul
import kotlin.String
import kotlin.collections.Map

public class GeneratedModel {
    public fun predict(inputTensors: Map<String, NDArray>): Map<String, NDArray> {
        val emptyNDArray: NDArray = IntNDArray(shape = intArrayOf(0))
        val tensors: Array<NDArray> = Array(7) { emptyNDArray }
        tensors[0] = inputTensors["x"] ?: error("Input tensor 'x' not provided")
        tensors[1] = inputTensors["y"] ?: error("Input tensor 'y' not provided")
        run {
            val a = tensors[0] // x
            val b = tensors[1] // y
            require(a is NumberNDArray && b is NumberNDArray)
            tensors[2] = a * b // mul_result
            // For garbage collector
            tensors[0] = emptyNDArray
            tensors[1] = emptyNDArray
        }
        run {
            tensors[3] = FloatNDArray(
                array = FloatTiledArray(
                    this::class.java.getResource(
                        "/onnx/initializers/org/example/GeneratedModel/add_model_param.bin"
                    )!!.readBytes().toFloatTiledArray(
                        blockSize = 5, blocksNum = 1
                    )
                ),
                strides = Strides(shape = intArrayOf(1, 5))
            ) // add_model_param [initializer]
            val a = tensors[2] // mul_result
            val b = tensors[3] // add_model_param
            require(a is NumberNDArray && b is NumberNDArray)
            tensors[4] = a + b // add_result
            // For garbage collector
            tensors[2] = emptyNDArray
            tensors[3] = emptyNDArray
        }
        run {
            tensors[5] = FloatNDArray(
                array = FloatTiledArray(
                    this::class.java.getResource(
                        "/onnx/initializers/org/example/GeneratedModel/matmul_model_param.bin"
                    )!!.readBytes().toFloatTiledArray(
                        blockSize = 10, blocksNum = 10
                    )
                ),
                strides = Strides(shape = intArrayOf(2, 5, 10))
            ) // matmul_model_param [initializer]
            val operator = MatMul(
                attributes = mapOf(),
                inputs = listOf("add_result", "matmul_model_param"),
                outputs = listOf("output"),
            )
            val inputs = listOf(
                tensors[4].asTensor("add_result"),
                tensors[5].asTensor("matmul_model_param"),
            )
            val result = operator.apply(Context(), inputs)
            val resultMap = operator.outputs.zip(result.map { it?.data }).toMap()
            tensors[6] = resultMap["output"] ?:
                    error("Required output 'output' not provided by 'MatMul' operator")
            // For garbage collector
            tensors[5] = emptyNDArray
            tensors[4] = emptyNDArray
        }
        return mapOf(
            "output" to tensors[6],
        )
    }
}
