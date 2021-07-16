import tanvd.kosogor.proxy.publishJar

group = rootProject.group
version = rootProject.version

dependencies {
    api("io.kinference", "inference", "0.1.2")
}

publishJar {}
