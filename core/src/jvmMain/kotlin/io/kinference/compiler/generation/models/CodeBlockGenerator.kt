package io.kinference.compiler.generation.models

import com.squareup.kotlinpoet.CodeBlock

abstract class CodeBlockGenerator : Generator<CodeBlock>() {
    protected val builder = CodeBlock.builder()

    override fun generate(): CodeBlock {
        generateImpl()
        return builder.build()
    }
}
