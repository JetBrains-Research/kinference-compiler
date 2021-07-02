package io.kinference.compiler.plugin

import com.google.auto.service.AutoService
import io.kinference.compiler.plugin.cli.ConfigurationKeys
import io.kinference.compiler.plugin.cli.PluginInfo.ENABLED_OPTION_INFO
import io.kinference.compiler.plugin.cli.PluginInfo.PLUGIN_ID
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

@Suppress("unused")
@AutoService(CommandLineProcessor::class)
class KInferenceCompilerCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String
        get() = PLUGIN_ID
    override val pluginOptions: Collection<AbstractCliOption>
        get() = listOf(ENABLED_OPTION)

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) =
        when (option) {
            ENABLED_OPTION -> configuration.put(ConfigurationKeys.ENABLED, value.toBoolean())
            else -> error("Unexpected configuration option ${option.optionName}")
        }

    companion object {
        val ENABLED_OPTION = CliOption(
            optionName = ENABLED_OPTION_INFO.name,
            valueDescription = ENABLED_OPTION_INFO.valueDescription,
            description = ENABLED_OPTION_INFO.description
        )
    }
}
