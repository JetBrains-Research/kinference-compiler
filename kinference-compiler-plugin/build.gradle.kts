import tanvd.kosogor.proxy.publishJar

group = rootProject.group
version = rootProject.version

plugins {
    kotlin("kapt")
}

dependencies {
    implementation(project(":kinference-compiler-core"))

    implementation(kotlin("compiler-embeddable"))

    implementation("com.google.auto.service", "auto-service-annotations", "1.0")
    kapt("com.google.auto.service", "auto-service", "1.0")

    testImplementation(gradleTestKit())

    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.7.0")
    testImplementation("org.junit.jupiter", "junit-jupiter-params", "5.7.0")
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", "5.7.0")
}

tasks.withType<Test> {
    useJUnitPlatform {
        includeTags = setOf()
    }

    testLogging {
        events("passed", "skipped", "failed")
    }
}

publishJar {}
