group = rootProject.group
version = rootProject.version

plugins {
    kotlin("kapt") apply true
}

kotlin {
    jvm {}

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":api"))

                implementation("com.squareup:kotlinpoet:1.9.0")
                implementation("io.kinference:inference:0.1.4")
            }
        }
    }
}
