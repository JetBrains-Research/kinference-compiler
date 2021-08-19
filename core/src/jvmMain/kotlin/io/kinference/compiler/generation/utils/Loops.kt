package io.kinference.compiler.generation.utils

import com.squareup.kotlinpoet.CodeBlock
import io.kinference.ndarray.Strides

fun CodeBlock.Builder.generateLoop(
    indexName: String,
    bounds: Pair<Any, Any>,
    indexStorage: IndexStorage,
    scopeNeeded: Boolean = false,
    body: CodeBlock.Builder.() -> Unit
) {
    indexStorage.put(indexName, bounds)
    when {
        !singleStepLoop(bounds) -> {
            withControlFlow("for ($indexName in ${bounds.first} until ${bounds.second})") {
                body()
            }
        }
        scopeNeeded -> {
            withControlFlow("run") {
                body()
            }
        }
        else -> {
            body()
        }
    }
}

fun CodeBlock.Builder.generateNestedLoops(
    indexName: (Int) -> String,
    bounds: List<Pair<Any, Any>>,
    indexStorage: IndexStorage,
    scopeNeeded: Boolean = false,
    body: CodeBlock.Builder.() -> Unit
) {
    var loopsGenerated = 0
    bounds.forEachIndexed { index, loopBounds ->
        indexStorage.put(indexName(index), loopBounds)
        if (!singleStepLoop(loopBounds)) {
            beginControlFlow("for (${indexName(index)} in ${loopBounds.first} until ${loopBounds.second})")
            loopsGenerated++
        }
    }
    if (loopsGenerated == 0 && scopeNeeded) {
        beginControlFlow("run")
        loopsGenerated++
    }
    body()
    repeat(loopsGenerated) {
        endControlFlow()
    }
}

fun blockIndex(
    strides: Strides,
    mainIndices: (Int) -> String,
    blockOffset: String,
    indexStorage: IndexStorage
): String {
    val indexedStrides = strides.shape.withIndex().filter { it.value != 1 }.map { (index, _) ->
        index to strides.strides[index]
    }
    val expression = indexedStrides.map { (index, stride) ->
        indexStorage.inline(mainIndices(index)).toLiteral() * stride.toLiteral()
    }.toAddExpression() * indexStorage.bounds(blockOffset).second.toLiteral() + indexStorage.inlineLiteral(blockOffset)
    return expression.toString()
}

fun singleStepLoop(bounds: Pair<Any, Any>): Boolean {
    val (lowerBound, upperBound) = bounds
    val lowerNum = lowerBound.toLiteral().toString().toIntOrNull()
    val upperNum = upperBound.toLiteral().toString().toIntOrNull()
    return lowerNum != null && upperNum != null && lowerNum + 1 == upperNum
}

fun String.inlineIndex(bounds: Pair<Any, Any>): String {
    if (singleStepLoop(bounds)) {
        return bounds.first.toLiteral().toString()
    }
    return this
}
