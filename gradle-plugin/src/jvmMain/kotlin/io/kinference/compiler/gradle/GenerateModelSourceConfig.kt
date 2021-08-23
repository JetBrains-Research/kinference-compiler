package io.kinference.compiler.gradle

import io.kinference.compiler.gradle.GenerateModelSourceConfig.Companion.NOT_SET
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import java.io.File

class GenerateModelSourceConfig {
    var modelFile: File = NOT_SET
    var outputDirectory: File = NOT_SET
    var implementationClass: String = ""
    var profile: Boolean = false

    internal var sourceDirectory: File = NOT_SET
    internal var resourceDirectory: File = NOT_SET

    internal companion object {
        val NOT_SET = File("")
    }
}

@Suppress("UNUSED")
fun Project.generateModelSource(configuration: GenerateModelSourceConfig.() -> Unit) {
    val config = GenerateModelSourceConfig().apply(configuration)

    when {
        config.modelFile === NOT_SET -> throw InvalidUserDataException("modelFile not set")
        !config.modelFile.exists() -> throw InvalidUserDataException("File '${config.modelFile}' does not exist")
        !config.modelFile.isFile -> throw InvalidUserDataException("'${config.modelFile}' is a directory")

        config.implementationClass.isEmpty() -> throw InvalidUserDataException("implementationClass not set")
    }
    if (config.outputDirectory === NOT_SET) {
        config.outputDirectory = File(buildDir, "generated/onnx")
    }

    config.sourceDirectory = config.outputDirectory.resolve("kotlin")
    config.resourceDirectory = config.outputDirectory.resolve("resources")

    (extensions.findByName("kotlin") as KotlinProjectExtension).apply {
        sourceSets.findByName("main")?.apply {
            kotlin.srcDir(config.sourceDirectory)
        }
    }
    extensions.findByType(SourceSetContainer::class.java)?.apply {
        findByName("main")?.apply {
            resources.srcDir(config.resourceDirectory)
        }
    }
    project.tasks.findByName("compileKotlin")?.dependsOn(
        KInferenceCompilerGradlePlugin.generateModelSourceTaskName
    )

    project.tasks.withType(GenerateModelSourceTask::class.java).single().configurations.add(config)
}
