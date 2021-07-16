package io.kinference.compiler

import io.kinference.data.ONNXData
import io.kinference.data.tensors.Tensor
import io.kinference.graph.Context
import io.kinference.operators.Operator
import io.kinference.types.ValueInfo

class CompileTimeModelImpl(
    private val operators: List<Operator<ONNXData, ONNXData>>,
    private val outputs: List<ValueInfo>,
    private val initializers: List<Tensor>,
    private val valueOrderInfo: Map<String, Int>,
    private val availableInputs: List<String>
) : CompileTimeModel() {
    override fun predict(inputs: Collection<ONNXData>): List<ONNXData> {
        val context = Context(null)
        for (tensor in initializers) {
            context.putValue(tensor.info.name, tensor)
        }
        for (input in inputs) {
            if (input.info.name !in availableInputs) {
                // TODO warning
                continue
            }
            context.putValue(input.info.name, input)
        }

        for ((i, operator) in operators.withIndex()) {
            val outputs = operator.applyWithCheck(context, operator.inputs.map { input -> if (input.isEmpty()) null else context.getValue(input) })

            context.cleanupUntilOrder(i)
            outputs.zip(operator.outputs) { output, variable ->
                if (output == null) require(variable.isEmpty()) { "Required output '$variable' not provided by '${operator.info.name}' operator" }
                if (variable.isNotEmpty()) {
                    context.putValue(variable, output!!.rename(name = variable))
                }
            }
        }
        return outputs.map { context.getValue(it.name) }
    }

    private fun Context.cleanupUntilOrder(order: Int) {
        return this.removeValues { valueOrderInfo.getOrDefault(it, Int.MAX_VALUE) <= order }
    }
}
