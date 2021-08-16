rootProject.name = "kinference-compiler"

include(":core")
include(":gradle-plugin")
include(":api")

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        jcenter()
        maven(url = "https://packages.jetbrains.team/maven/p/ki/maven")
    }
}
