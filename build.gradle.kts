import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

group = "io.kinference.compiler"
version = "0.1.0"

plugins {
    id("tanvd.kosogor") version "1.0.12" apply true
    kotlin("jvm") version "1.5.20" apply true
    kotlin("kapt") version "1.5.20" apply false
    id("com.github.gmazzo.buildconfig") version "3.0.1" apply false
}

allprojects {
    apply {
        plugin("kotlin")
    }

    tasks.withType<KotlinJvmCompile> {
        kotlinOptions {
            jvmTarget = "11"
            languageVersion = "1.5"
            apiVersion = "1.5"
        }
    }

    repositories {
        google()
    }
}
