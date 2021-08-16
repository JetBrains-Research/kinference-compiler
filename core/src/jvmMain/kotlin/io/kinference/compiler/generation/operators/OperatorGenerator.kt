package io.kinference.compiler.generation.operators

import com.squareup.kotlinpoet.CodeBlock
import io.kinference.compiler.generation.operators.common.DefaultOperatorGenerator
import io.kinference.compiler.generation.operators.math.AddGenerator
import io.kinference.compiler.generation.operators.math.MatMulGenerator
import io.kinference.compiler.generation.operators.math.MulGenerator
import io.kinference.compiler.generation.operators.math.SubGenerator
import io.kinference.operators.Operator
import io.kinference.operators.math.Add
import io.kinference.operators.math.MatMul
import io.kinference.operators.math.Mul
import io.kinference.operators.math.Sub
import kotlin.time.ExperimentalTime

/* Entry point for operators generation.
 * Each implemented operator must be added here.
 */
@OptIn(ExperimentalTime::class)
class OperatorGenerator(
    private val operator: Operator<*, *>,
    info: OperatorGenerationInfo
) : BaseOperatorGenerator(info) {
    override fun generateImpl() {}

    override fun generate(): CodeBlock =
        when (operator) {
            is Add -> AddGenerator(operator, info).generate()
            is MatMul -> MatMulGenerator(operator, info).generate()
            is Mul -> MulGenerator(operator, info).generate()
            is Sub -> SubGenerator(operator, info).generate()
            else -> DefaultOperatorGenerator(operator, info).generate()
        }
}
