package io.kinference.compiler.generation.utils

import com.squareup.kotlinpoet.CodeBlock
import io.kinference.ndarray.Strides

fun CodeBlock.Builder.generateLoop(
    indexName: String, lowerBound: Any, upperBound: Any, toGenerate: Boolean = true, body: CodeBlock.Builder.() -> Unit
) {
    if (toGenerate) {
        beginControlFlow("for ($indexName in $lowerBound until $upperBound)")
        body()
        endControlFlow()
    } else {
        body()
    }
}

fun CodeBlock.Builder.generateNestedLoops(
    indexName: (Int) -> String, bounds: List<Pair<Any, Any>>, body: CodeBlock.Builder.() -> Unit
) {
    bounds.forEachIndexed { index, (lowerBound, upperBound) ->
        beginControlFlow("for (${indexName(index)} in $lowerBound until $upperBound)")
    }
    body()
    repeat(bounds.size) {
        endControlFlow()
    }
}

fun blockIndex(
    strides: Strides, mainIndices: (Int) -> Any, blocksInRow: Any, offset: Any, oneBlockInRow: Boolean
): String {
    val indexedStrides = strides.shape.withIndex().filter { it.value != 1 }.map { (index, _) ->
        index to strides.strides[index]
    }
    val blocksInRowLiteral = (if (oneBlockInRow) 1 else blocksInRow).toLiteral()
    val offsetLiteral = (if (oneBlockInRow) 0 else offset).toLiteral()
    return (indexedStrides.map { (index, stride) ->
        mainIndices(index).toLiteral() * stride.toLiteral()
    }.toAddExpression() * blocksInRowLiteral + offsetLiteral).toString()
}
