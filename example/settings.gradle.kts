rootProject.name = "example"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        jcenter()
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "io.kinference.compiler") {
                useModule("io.kinference.compiler:gradle-plugin-jvm:${requested.version}")
            }
        }
    }
}
