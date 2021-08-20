import io.kinference.compiler.gradle.generateModelSource
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
    kotlin("jvm") version "1.4.30"
    kotlin("kapt") version "1.4.30"
    id("io.kinference.compiler") version "0.1.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    jcenter()
    maven(url = "https://packages.jetbrains.team/maven/p/ki/maven")
}

dependencies {
    implementation("io.kinference.compiler", "api", "0.1.0")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.7.0")
    testImplementation("org.junit.jupiter", "junit-jupiter-params", "5.7.0")
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", "5.7.0")

    testImplementation("org.openjdk.jmh", "jmh-core", "1.25.1")
    kaptTest("org.openjdk.jmh", "jmh-generator-annprocess", "1.25.1")
}

tasks.withType<KotlinJvmCompile> {
    kotlinOptions {
        jvmTarget = "11"
        languageVersion = "1.4"
        apiVersion = "1.4"
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=kotlin.ExperimentalUnsignedTypes"
        )
    }
}

val testResources = projectDir.resolve("src/test/resources/test_data")

testResources.list()?.forEach { testSuitName ->
    val testSuitDirectory = testResources.resolve(testSuitName)
    testSuitDirectory.list()?.forEach { testName ->
        val testDirectory = testSuitDirectory.resolve(testName)
        generateModelSource {
            modelFile = testDirectory.resolve("model.onnx")
            implementationClass = File(testDirectory, "model_class_name.txt").readText()
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform {
        includeTags = setOf("correctness")
    }

    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.create("benchmark", Test::class.java) {
    useJUnitPlatform {
        includeTags = setOf("benchmark")
    }

    testLogging {
        events("passed", "skipped", "failed")
    }
}
