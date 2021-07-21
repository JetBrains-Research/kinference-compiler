group = rootProject.group
version = rootProject.version

plugins {
    id("com.gradle.plugin-publish") version "0.12.0"
}

kotlin {
    jvm {}

    sourceSets {
        val jvmMain by getting {
            dependencies {
                api(files(org.gradle.kotlin.dsl.provider.gradleKotlinDslOf(project)))
                compileOnly(kotlin("gradle-plugin"))
                implementation(project(":core"))
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
