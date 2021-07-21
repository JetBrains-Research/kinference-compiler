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

                implementation("io.kinference:inference:0.1.2")
                implementation("com.squareup:kotlinpoet:1.9.0")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit5"))

                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")
            }
        }
    }
}
