package io.kinference.compiler.generation.operators

import com.squareup.kotlinpoet.CodeBlock
import io.kinference.compiler.generation.operators.math.AddGenerator
import io.kinference.compiler.generation.operators.math.MulGenerator
import io.kinference.operators.Operator
import io.kinference.operators.math.Add
import io.kinference.operators.math.Mul

class OperatorGenerator(
    private val operator: Operator<*, *>,
    nameMapping: (String) -> String
) : BaseOperatorGenerator(nameMapping) {
    override fun generateImpl() {}

    override fun generate(): CodeBlock =
        when (operator) {
            is Add -> AddGenerator(operator, nameMapping).generate()
            is Mul -> MulGenerator(operator, nameMapping).generate()
            else -> DefaultOperatorGenerator(operator, nameMapping).generate()
        }
}
