package io.kinference.compiler.example

import io.kinference.compiler.CompileTimeModel
import io.kinference.data.tensors.Tensor
import io.kinference.onnx.TensorProto
import java.io.File

fun main() {
    // Insert your path to model
    val dir = "/home/isomethane/test/make_easy_onnx_model/make_easy_onnx_model"

    val inputPaths = listOf("input_0.pb", "input_1.pb").map { File(dir, it) }
    val outputPath = File(dir, "output_0.pb")

    // Argument must contain full path to model
    // Insert your path to model
    val compiledModel = CompileTimeModel("/home/isomethane/test/make_easy_onnx_model/make_easy_onnx_model/model.onnx")

    val input = inputPaths.map { TensorProto.ADAPTER.decode(it.readBytes()) }.map { Tensor.create(it) }
    val expectedOutput = Tensor.create(TensorProto.ADAPTER.decode(outputPath.readBytes()))
    val output = compiledModel.predict(input)

    println("...")
}
