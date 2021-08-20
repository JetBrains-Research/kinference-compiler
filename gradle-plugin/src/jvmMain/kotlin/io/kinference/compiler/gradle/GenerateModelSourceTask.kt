package io.kinference.compiler.gradle

import io.kinference.compiler.generation.ModelSourceGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/* Generates source code for ONNX models specified in configuration.
 * Each model is configured separately.
 * Example usage:
 * generateModelSource {
 *     modelFile = File(projectDir, "src/main/resources/comment_updater/model.onnx")
 *     implementationClass = "org.example.CommentUpdaterGeneratedModel"
 * }
 */
open class GenerateModelSourceTask : DefaultTask() {
    val configurations: MutableList<GenerateModelSourceConfig> = ArrayList()

    @TaskAction
    fun action() {
        configurations.forEach {
            ModelSourceGenerator(
                modelFile = it.modelFile,
                sourceDirectory = it.sourceDirectory,
                resourceDirectory = it.resourceDirectory,
                implementationClass = it.implementationClass,
                implementProfiling = it.profile
            ).generate()
        }
    }
}
