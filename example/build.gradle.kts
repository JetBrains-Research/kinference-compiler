import io.kinference.compiler.gradle.generateModelSource

plugins {
    kotlin("jvm") version "1.4.30"
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
}
