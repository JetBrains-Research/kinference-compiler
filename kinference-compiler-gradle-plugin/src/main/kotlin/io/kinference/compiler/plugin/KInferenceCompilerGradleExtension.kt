package io.kinference.compiler.plugin

import org.gradle.api.Project

/** KInference Compiler plugin configuration */
open class KInferenceCompilerGradleExtension {
    /** If [false], this plugin won't actually be applied */
    var enabled: Boolean = true
}

internal val Project.kInferenceCompiler: KInferenceCompilerGradleExtension
    get() = project.extensions.findByType(KInferenceCompilerGradleExtension::class.java) ?: kotlin.run {
        extensions.create("kInferenceCompiler", KInferenceCompilerGradleExtension::class.java)
    }

/**
 * KInference Compiler plugin configuration generator.
 * Plugin may be configured in build.gradle.kts file. For example:
 * kInferenceCompiler {
 *   enabled = false
 *   // ... other options from KInferenceCompilerGradleExtension class
 * }
 */
@Suppress("unused")
fun Project.kInferenceCompiler(configure: KInferenceCompilerGradleExtension.() -> Unit) {
    kInferenceCompiler.apply(configure)
}
