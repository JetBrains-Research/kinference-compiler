package io.kinference.compiler.plugin

import io.kinference.compiler.plugin.cli.PluginInfo.ENABLED_OPTION_INFO
import io.kinference.compiler.plugin.cli.PluginInfo.GRADLE_ARTIFACT_ID
import io.kinference.compiler.plugin.cli.PluginInfo.GRADLE_GROUP_ID
import io.kinference.compiler.plugin.cli.PluginInfo.PLUGIN_ID
import io.kinference.compiler.plugin.cli.PluginInfo.VERSION
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

@Suppress("unused")
class KInferenceCompilerSubPlugin: KotlinCompilerPluginSupportPlugin {
    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val extension = project.kInferenceCompiler

        return project.provider {
            listOf(SubpluginOption(key = ENABLED_OPTION_INFO.name, value = extension.enabled.toString()))
        }
    }

    override fun getCompilerPluginId(): String = PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = GRADLE_GROUP_ID,
        artifactId = GRADLE_ARTIFACT_ID,
        version = VERSION
    )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true
}
