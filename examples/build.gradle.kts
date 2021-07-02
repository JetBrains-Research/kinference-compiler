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

dependencies {
}

repositories {
    mavenCentral()
    mavenLocal()
    google()
}
