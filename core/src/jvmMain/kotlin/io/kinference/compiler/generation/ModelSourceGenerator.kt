package io.kinference.compiler.generation

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import io.kinference.compiler.api.GeneratedONNXModel
import java.io.File

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

        val implementationClassName = ClassName(
            implementationClass.replaceAfterLast(".", "").dropLast(1),
            implementationClass.substringAfterLast(".")
        )

        implementationClassFile.writeText(
            FileSpec.builder(implementationClassName.packageName, implementationClassName.simpleName)
                .addType(TypeSpec.classBuilder(implementationClassName).build())
                .build()
                .toString()
        )
    }
}
