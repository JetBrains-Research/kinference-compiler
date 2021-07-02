import tanvd.kosogor.proxy.publishJar
import tanvd.kosogor.proxy.publishPlugin

group = rootProject.group
version = rootProject.version

plugins {
    kotlin("kapt")
}

dependencies {
    implementation(project(":kinference-compiler-core"))

    implementation(kotlin("gradle-plugin-api"))
    implementation(kotlin("compiler-embeddable"))
}

publishPlugin {
    id = "io.kinference.compiler"
    displayName = "KInference Compiler"
    implementationClass = "io.kinference.compiler.plugin.KInferenceCompilerSubPlugin"
    version = project.version.toString()

    info {
        description = "Library for building ONNX models in compile-time"
        website = "https://github.com/JetBrains-Research/kinference-compiler"
        vcsUrl = "https://github.com/JetBrains-Research/kinference-compiler"
        tags.addAll(listOf("kotlin", "compile-time", "onnx"))
    }
}

publishJar {
    publication {
        artifactId = "io.kinference.compiler.gradle-plugin"
    }
}
