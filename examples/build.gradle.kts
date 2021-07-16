import io.kinference.compiler.plugin.kInferenceCompiler
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

group = rootProject.group
version = rootProject.version

plugins {
    id("io.kinference.compiler") version "0.1.0" apply true
    kotlin("jvm") version "1.5.20" apply true
}

tasks.withType<KotlinJvmCompile> {
    kotlinOptions {
        jvmTarget = "11"
        languageVersion = "1.5"
        apiVersion = "1.5"
    }
}

kInferenceCompiler {
    enabled = true
}

dependencies {
    implementation("io.kinference.compiler", "kinference-compiler-dsl", "0.1.0")
}

repositories {
    mavenCentral()
    mavenLocal()
    google()
    jcenter()
}
