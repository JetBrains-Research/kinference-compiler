package org.example

import io.kinference.data.tensors.Tensor
import io.kinference.model.Model
import io.kinference.ndarray.arrays.FloatNDArray
import io.kinference.protobuf.message.TensorProto
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main() {
    val x = Tensor.create(TensorProto.decode(object {}::class.java.getResource("/make_easy_onnx_model/input_0.pb")!!.readBytes()))
    val y = Tensor.create(TensorProto.decode(object {}::class.java.getResource("/make_easy_onnx_model/input_1.pb")!!.readBytes()))

    val modelPath = object {}::class.java.getResource("/make_easy_onnx_model/model.onnx")!!
    val model = Model.load(modelPath.readBytes())
    val generatedModel = GeneratedModel()

    val modelOutput: FloatNDArray = (model.predict(listOf(x, y))[0] as Tensor).data as FloatNDArray
    val generatedModelOutput = generatedModel.predict(mapOf("x" to x.data, "y" to y.data))["output"]!! as FloatNDArray

    println(modelOutput.array.blocks.contentDeepEquals(generatedModelOutput.array.blocks))
}
