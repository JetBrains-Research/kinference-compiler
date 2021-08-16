package io.kinference.compiler.generation

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import io.kinference.model.Model
import java.io.File
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class ModelSourceGenerator(
    private val modelFile: File,
    private val sourceDirectory: File,
    private val resourceDirectory: File,
    private val implementationClass: String
) {
    fun generate() {
        sourceDirectory.mkdirs()
        resourceDirectory.mkdirs()

        val implementationClassPath = implementationClass.replace(".", "/")
        val implementationClassFile = sourceDirectory.resolve("$implementationClassPath.kt")
        implementationClassFile.parentFile.mkdirs()
        implementationClassFile.createNewFile()

        val model = Model.load(modelFile.readBytes())

        val implementationClassName = ClassName(
            implementationClass.replaceAfterLast(".", "").dropLast(1),
            implementationClass.substringAfterLast(".")
        )
        val modelClass = ModelClassGenerator(model.graph, resourceDirectory, implementationClassName).generate()

        implementationClassFile.writeText(
            FileSpec.builder(implementationClassName.packageName, implementationClassName.simpleName)
                .indent(indent)
                .addType(modelClass)
                .build()
                .toString()
        )
    }

    companion object {
        const val indent = "    "
    }
}
