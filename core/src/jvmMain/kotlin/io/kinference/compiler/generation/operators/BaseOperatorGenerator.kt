package io.kinference.compiler.generation.operators

import io.kinference.compiler.generation.info.TensorInfo
import io.kinference.compiler.generation.models.CodeBlockGenerator
import io.kinference.compiler.generation.models.ListInitBuilder
import io.kinference.operators.Operator
import kotlin.time.ExperimentalTime

/* Base class for operator generators.
 * Final classes that inherit this class are meant to be added to list in OperatorGenerator.
 */
@OptIn(ExperimentalTime::class)
abstract class BaseOperatorGenerator(
    private val operator: Operator<*, *>,
    protected val info: OperatorGenerationInfo
) : CodeBlockGenerator() {
    protected val nameMapping: (String) -> String = info.nameMapping
    protected val tensorInfo: MutableMap<String, TensorInfo> = info.tensorInfo
    protected val operatorsListBuilder: ListInitBuilder = info.operatorsListBuilder

    protected fun isLastUsage(inputIndex: Int): Boolean =
        info.tensorLastUsageIndex(operator.inputs[inputIndex]) == info.operatorIndex
}

data class OperatorGenerationInfo(
    val nameMapping: (String) -> String,
    val tensorLastUsageIndex: (String) -> Int,
    val tensorInfo: MutableMap<String, TensorInfo>,
    val operatorsListBuilder: ListInitBuilder,
    val operatorIndex: Int
)
