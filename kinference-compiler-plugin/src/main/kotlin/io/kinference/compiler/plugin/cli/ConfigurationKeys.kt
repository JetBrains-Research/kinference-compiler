package io.kinference.compiler.plugin.cli

import org.jetbrains.kotlin.config.CompilerConfigurationKey

object ConfigurationKeys {
    val ENABLED = CompilerConfigurationKey<Boolean>("${PluginInfo.PLUGIN_ID}.enabled")
}
