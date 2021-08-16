package io.kinference.compiler.api

import io.kinference.ndarray.arrays.NDArray

interface GeneratedONNXModel {
    fun predict(inputTensors: Map<String, NDArray>): Map<String, NDArray>
}
