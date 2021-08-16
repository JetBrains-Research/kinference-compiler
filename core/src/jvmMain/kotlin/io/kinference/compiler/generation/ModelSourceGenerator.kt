package io.kinference.compiler.generation

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
    }
}
