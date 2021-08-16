rootProject.name = "tests"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        jcenter()
        maven(url = "https://packages.jetbrains.team/maven/p/ki/maven")
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "io.kinference.compiler") {
                useModule("io.kinference.compiler:gradle-plugin-jvm:${requested.version}")
            }
        }
    }
}
