package io.kinference.compiler.plugin

import com.google.auto.service.AutoService
import io.kinference.compiler.plugin.cli.ConfigurationKeys
import io.kinference.compiler.plugin.generation.ExampleIrGenerationExtension
import io.kinference.compiler.plugin.utils.initMessageCollector
import io.kinference.compiler.plugin.utils.messageCollector
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

@Suppress("unused")
@AutoService(ComponentRegistrar::class)
class KInferenceCompilerComponentRegistrar : ComponentRegistrar {
    // The path will be: pathToKotlin/daemon/kinference-compiler.log
    private val logFilePath = "kinference-compiler.log"

    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        if (configuration[ConfigurationKeys.ENABLED] != true) {
            return
        }
        configuration.initMessageCollector(logFilePath)

        IrGenerationExtension.registerExtension(
            project,
            ExampleIrGenerationExtension(messageCollector = configuration.messageCollector)
        )
    }
}
