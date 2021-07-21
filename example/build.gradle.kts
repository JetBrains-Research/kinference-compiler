import io.kinference.compiler.gradle.generateModelSource

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
}

generateModelSource {
    modelFile = File(projectDir, "src/main/resources/make_easy_onnx_model/model.onnx")
    implementationClass = "org.example.GeneratedModel"
}

dependencies {
    implementation("io.kinference.compiler", "api", "0.1.0")
    testImplementation("org.openjdk.jmh:jmh-core:1.25.1")
    kaptTest("org.openjdk.jmh:jmh-generator-annprocess:1.25.1")
    testImplementation(kotlin("test-junit5"))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")
}

tasks.test {
    useJUnitPlatform()
}