package io.kinference.compiler.utils

import io.kinference.compiler.api.GeneratedONNXModel
import io.kinference.data.tensors.Tensor
import io.kinference.model.Model
import io.kinference.protobuf.message.TensorProto
import java.io.File
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
data class TestData(
    val defaultModel: Model,
    val generatedModel: GeneratedONNXModel,
    val inputs: List<Tensor>,
    val outputs: List<Tensor>
)

@OptIn(ExperimentalTime::class)
fun getTestData(testDirectory: File, testDataSet: File): TestData =
    TestData(
        defaultModel = Model.load(testDirectory.resolve("model.onnx").readBytes()),
        generatedModel = Class.forName(
            testDirectory.resolve("model_class_name.txt").readText()
        ).getConstructor().newInstance() as GeneratedONNXModel,
        inputs = testDataSet.listFiles { _, name -> "input_" in name }!!.map {
            Tensor.create(TensorProto.decode(it.readBytes()))
        },
        outputs = testDataSet.listFiles { _, name -> "output_" in name }!!.map {
            Tensor.create(TensorProto.decode(it.readBytes()))
        }
    )
