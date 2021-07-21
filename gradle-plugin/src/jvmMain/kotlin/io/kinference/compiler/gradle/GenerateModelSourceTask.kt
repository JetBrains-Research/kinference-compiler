package io.kinference.compiler.gradle

import io.kinference.compiler.generation.ModelSourceGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class GenerateModelSourceTask : DefaultTask() {
    val configurations: MutableList<GenerateModelSourceConfig> = ArrayList()

    @TaskAction
    fun action() {
        configurations.forEach {
            ModelSourceGenerator(
                modelFile = it.modelFile,
                sourceDirectory = it.sourceDirectory,
                resourceDirectory = it.resourceDirectory,
                implementationClass = it.implementationClass
            ).generate()
        }
    }
}
