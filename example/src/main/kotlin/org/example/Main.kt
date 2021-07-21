package org.example

import io.kinference.data.tensors.Tensor
import io.kinference.model.Model
import io.kinference.onnx.TensorProto

fun main() {
    val x = Tensor.create(TensorProto.ADAPTER.decode(object {}::class.java.getResource("/make_easy_onnx_model/input_0.pb")!!.readBytes()))
    val y = Tensor.create(TensorProto.ADAPTER.decode(object {}::class.java.getResource("/make_easy_onnx_model/input_1.pb")!!.readBytes()))

    val modelPath = object {}::class.java.getResource("/make_easy_onnx_model/model.onnx")!!
    val model = Model.load(modelPath.file)
    val generatedModel = GeneratedModel()

    val modelOutput = model.predict(listOf(x, y))
    val generatedModelOutput = generatedModel.predict(mapOf("x" to x.data, "y" to y.data))

    println("...")
}
