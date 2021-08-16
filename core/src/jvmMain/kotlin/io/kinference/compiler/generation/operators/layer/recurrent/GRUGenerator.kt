package io.kinference.compiler.generation.operators.layer.recurrent

import io.kinference.compiler.generation.info.TensorInfo
import io.kinference.compiler.generation.operators.OperatorGenerationInfo
import io.kinference.compiler.generation.operators.common.TypeAndShapeAwareOperatorGenerator
import io.kinference.operators.layer.recurrent.gru.GRU
import kotlin.time.ExperimentalTime

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
