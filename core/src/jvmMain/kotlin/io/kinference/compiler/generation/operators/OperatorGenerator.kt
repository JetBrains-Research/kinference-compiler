package io.kinference.compiler.generation.operators

import com.squareup.kotlinpoet.CodeBlock
import io.kinference.compiler.generation.operators.common.DefaultOperatorGenerator
import io.kinference.compiler.generation.operators.flow.WhereGenerator
import io.kinference.compiler.generation.operators.layer.recurrent.GRUGenerator
import io.kinference.compiler.generation.operators.logical.EqualGenerator
import io.kinference.compiler.generation.operators.logical.GreaterGenerator
import io.kinference.compiler.generation.operators.logical.OrGenerator
import io.kinference.compiler.generation.operators.math.AddGenerator
import io.kinference.compiler.generation.operators.math.MatMulGenerator
import io.kinference.compiler.generation.operators.math.MulGenerator
import io.kinference.compiler.generation.operators.math.SubGenerator
import io.kinference.compiler.generation.operators.tensor.*
import io.kinference.operators.Operator
import io.kinference.operators.flow.Where
import io.kinference.operators.layer.recurrent.gru.GRU
import io.kinference.operators.logical.Equal
import io.kinference.operators.logical.Greater
import io.kinference.operators.logical.Or
import io.kinference.operators.math.Add
import io.kinference.operators.math.MatMul
import io.kinference.operators.math.Mul
import io.kinference.operators.math.Sub
import io.kinference.operators.tensor.*
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
            is Concat -> ConcatGenerator(operator, info).generate()
            is Equal -> EqualGenerator(operator, info).generate()
            is Gather -> GatherGenerator(operator, info).generate()
            is Greater -> GreaterGenerator(operator, info).generate()
            is GRU -> GRUGenerator(operator, info).generate()
            is MatMul -> MatMulGenerator(operator, info).generate()
            is Mul -> MulGenerator(operator, info).generate()
            is Or -> OrGenerator(operator, info).generate()
            is Reshape -> ReshapeGenerator(operator, info).generate()
            is Split -> SplitGenerator(operator, info).generate()
            is Sub -> SubGenerator(operator, info).generate()
            is Transpose -> TransposeGenerator(operator, info).generate()
            is Unsqueeze -> UnsqueezeGenerator(operator, info).generate()
            is Where -> WhereGenerator(operator, info).generate()
            else -> DefaultOperatorGenerator(operator, info).generate()
        }
}
