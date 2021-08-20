package io.kinference.compiler.generation.operators

import io.kinference.compiler.generation.info.TensorInfo
import io.kinference.compiler.generation.context.ContextBuilder
import io.kinference.compiler.generation.models.ListInitBuilder

data class OperatorGenerationInfo(
    val nameMapping: (String) -> String,
    val tensorLastUsageIndex: (String) -> Int,
    val tensorInfo: MutableMap<String, TensorInfo>,
    val operatorsListBuilder: ListInitBuilder,
    val preparedContextBuilder: ContextBuilder,
    val operatorIndex: Int
)
