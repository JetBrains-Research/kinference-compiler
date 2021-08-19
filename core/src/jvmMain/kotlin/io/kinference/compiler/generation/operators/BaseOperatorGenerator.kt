package io.kinference.compiler.generation.operators

import io.kinference.compiler.generation.info.TensorInfo
import io.kinference.compiler.generation.models.CodeBlockGenerator
import io.kinference.compiler.generation.models.ListInitBuilder

/* Base class for operator generators.
 * Final classes that inherit this class are meant to be added to list in OperatorGenerator.
 */
abstract class BaseOperatorGenerator(
    protected val info: OperatorGenerationInfo
) : CodeBlockGenerator() {
    protected val nameMapping: (String) -> String = info.nameMapping
    protected val tensorInfo: MutableMap<String, TensorInfo> = info.tensorInfo
    protected val operatorsListBuilder: ListInitBuilder = info.operatorsListBuilder
}

data class OperatorGenerationInfo(
    val nameMapping: (String) -> String,
    val tensorInfo: MutableMap<String, TensorInfo>,
    val operatorsListBuilder: ListInitBuilder
)
