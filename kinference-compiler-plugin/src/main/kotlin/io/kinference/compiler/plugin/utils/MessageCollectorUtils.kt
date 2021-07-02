package io.kinference.compiler.plugin.utils

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.*
import org.jetbrains.kotlin.config.CompilerConfiguration
import java.io.File
import java.io.PrintStream

fun CompilerConfiguration.initMessageCollector(filePath: String) {
    val file = File(filePath)
    file.createNewFile()
    this.put(
        CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
        PrintingMessageCollector(PrintStream(file.outputStream()), MessageRenderer.PLAIN_FULL_PATHS, true)
    )
}

val CompilerConfiguration.messageCollector: MessageCollector
    get() = this.get(
        CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
        MessageCollector.NONE
    )

fun MessageCollector.log(message: String) {
    this.report(
        CompilerMessageSeverity.LOGGING,
        "KInference Compiler: $message",
        CompilerMessageLocation.create(null)
    )
}
