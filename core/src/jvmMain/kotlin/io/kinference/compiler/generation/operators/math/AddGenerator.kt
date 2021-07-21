package io.kinference.compiler.generation.operators.math

import io.kinference.compiler.generation.operators.SimpleOperatorGenerator
import io.kinference.ndarray.arrays.NumberNDArray
import io.kinference.operators.math.Add

class AddGenerator(
    private val operator: Add,
    nameMapping: (String) -> String
) : SimpleOperatorGenerator(operator, nameMapping) {
    override fun operatorImpl() {
        val output = operator.outputs[0]
        builder.add(
            """
            |require(a is %T && b is %T)
            |${nameMapping(output)} = a + b // $output
            |""".trimMargin(),
            NumberNDArray::class,
            NumberNDArray::class
        )
    }
}
