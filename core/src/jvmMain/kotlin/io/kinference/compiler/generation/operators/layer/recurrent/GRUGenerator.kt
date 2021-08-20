package io.kinference.compiler.generation.operators.layer.recurrent

import io.kinference.compiler.generation.info.TensorInfo
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.operators.common.TypeAndShapeAwareOperatorGenerator
import io.kinference.data.tensors.Tensor
import io.kinference.data.tensors.asTensor
import io.kinference.operators.layer.recurrent.gru.GRU
import kotlin.time.ExperimentalTime

/**
 * GRU generator.
 *
 * [ONNX documentation](https://github.com/onnx/onnx/blob/master/docs/Operators.md#GRU)
 *
 * KInference class: [GRU]
 */
@OptIn(ExperimentalTime::class)
class GRUGenerator(
    private val operator: GRU,
    info: OperatorGenerationInfo
) : TypeAndShapeAwareOperatorGenerator(operator, info) {
    override val inputsToInfer: List<String> = listOf(operator.inputs.first())

    private val activations: List<String> = operator.getAttribute<List<String>>("activations").let {
        if (direction == "forward" || direction == "reverse")
            it.subList(0, 2)
        else
            it
    }
    private val direction: String = operator.getAttribute("direction")
    private val hiddenSize: Int = operator.getAttribute<Number>("hidden_size").toInt()
    private val batchWise: Boolean = operator.getAttribute<Number>("layout").toInt() == 1
    private val linearBeforeReset: Boolean = operator.getAttribute<Number>("linear_before_reset").toInt() == 1

    private val numLayers = if (direction == "bidirectional") 2 else 1

    private fun prepareWeights(tensor: Tensor): Tensor {
        val shape = tensor.data.shape
        val newShape = intArrayOf(shape[0], 3, shape[1] / 3, shape[2])
        return tensor.data.reshapeView(newShape).toMutable().transpose(intArrayOf(0, 1, 3, 2)).asTensor("prepared_${tensor.info.name}")
    }

    private fun prepareBias(tensor: Tensor): Tensor {
        val shape = tensor.data.shape
        val newShape = intArrayOf(shape[0], 6, shape[1] / 6)
        return tensor.data.toMutable().reshape(newShape).asTensor("prepared_${tensor.info.name}")
    }

    init {
        val weights = tensorInfo[operator.inputs[1]]
        val recurrentWeights = tensorInfo[operator.inputs[2]]
        val bias = tensorInfo[operator.inputs.getOrNull(3)]
        listOfNotNull(
            weights?.tensor?.let { prepareWeights(it) },
            recurrentWeights?.tensor?.let { prepareWeights(it) },
            bias?.tensor?.let { prepareBias(it) }
        ).forEach {
            preparedContextBuilder.addTensor(it.info.name, it)
        }
    }

    override fun resultInfo(): Map<String, TensorInfo> {
        val input = tensorInfo.getValue(operator.inputs.first())
        val seqLength = input.shape[0]
        val batchSize = input.shape[1]

        return mapOf(operator.outputs.first() to TensorInfo(
            shape = intArrayOf(seqLength, numLayers, batchSize, hiddenSize),
            dataType = input.dataType)
        )
    }
}
