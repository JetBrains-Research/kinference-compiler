package io.kinference.compiler.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class KInferenceCompilerGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.task(mapOf("type" to GenerateModelSourceTask::class.java), generateModelSourceTaskName)
    }

    companion object {
        const val generateModelSourceTaskName = "generateModelSource"
    }
}
