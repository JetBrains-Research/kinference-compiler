package io.kinference.compiler.generation.operators

import io.kinference.operators.Operator

abstract class SimpleOperatorGenerator(
    private val operator: Operator<*, *>,
    nameMapping: (String) -> String
) : BaseOperatorGenerator(nameMapping) {
    protected abstract fun operatorImpl()

    override fun generateImpl() {
        builder.apply {
            operator.inputs.forEachIndexed { index, input ->
                addStatement("val ${'a' + index} = ${nameMapping(input)} // $input")
            }
        }
        operatorImpl()
    }
}
