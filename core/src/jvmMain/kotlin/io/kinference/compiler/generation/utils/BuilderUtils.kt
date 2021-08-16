package io.kinference.compiler.generation.utils

import com.squareup.kotlinpoet.CodeBlock

fun CodeBlock.Builder.endLine() = apply {
    add(System.lineSeparator())
}

fun CodeBlock.Builder.addLine(codeBlock: CodeBlock) = apply {
    add(codeBlock)
    endLine()
}

fun CodeBlock.Builder.addLine(format: String, vararg args: Any?) = apply {
    add(format, *args)
    endLine()
}

fun CodeBlock.Builder.addNamedLine(format: String, arguments: Map<String, *>) = apply {
    addNamed(format, arguments)
    endLine()
}

fun CodeBlock.Builder.withControlFlow(
    controlFlow: String, vararg args: Any?, builderAction: CodeBlock.Builder.() -> Unit
) {
    beginControlFlow(controlFlow, *args)
    builderAction()
    endControlFlow()
}
