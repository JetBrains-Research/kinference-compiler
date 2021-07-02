package io.kinference.compiler.plugin.cli

data class CliOptionInfo(
    val name: String,
    val valueDescription: String,
    val description: String
)

object PluginInfo {
    const val PLUGIN_ID = "io.kinference.compiler"
    const val GRADLE_GROUP_ID = "io.kinference.compiler"
    const val GRADLE_ARTIFACT_ID = "kinference-compiler-plugin"
    const val VERSION = "0.1.0"

    val ENABLED_OPTION_INFO = CliOptionInfo(
        name = "enabled",
        valueDescription = "<true|false>",
        description = "Enables/disables plugin"
    )
}
