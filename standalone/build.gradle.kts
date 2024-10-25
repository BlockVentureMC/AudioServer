import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "8.1.1"

}

val dotenvVersion: String by project
val fruxzAscendVersion: String by project
val logbackVersion: String by project
val lavaPlayerVersion: String by project
val ktorVersion: String by project
val webrtcJavaVersion: String by project

val deps = listOf(
    "dev.fruxz:ascend:$fruxzAscendVersion",
    "io.github.cdimascio:dotenv-kotlin:$dotenvVersion",
    "ch.qos.logback:logback-classic:$logbackVersion",
    "dev.arbjerg:lavaplayer:$lavaPlayerVersion",

    // KTOR
    "io.ktor:ktor-server-netty:$ktorVersion",
    "io.ktor:ktor-server-websockets:$ktorVersion",
    "io.ktor:ktor-server-sse:$ktorVersion",

    "dev.onvoid.webrtc:webrtc-java:$webrtcJavaVersion",
    "dev.onvoid.webrtc:webrtc-java:$webrtcJavaVersion:windows-x86_64",
    "dev.onvoid.webrtc:webrtc-java:$webrtcJavaVersion:macos-x86_64",
    "dev.onvoid.webrtc:webrtc-java:$webrtcJavaVersion:macos-aarch64",
    "dev.onvoid.webrtc:webrtc-java:$webrtcJavaVersion:linux-x86_64",
    "dev.onvoid.webrtc:webrtc-java:$webrtcJavaVersion:linux-aarch64",
    "dev.onvoid.webrtc:webrtc-java:$webrtcJavaVersion:linux-aarch32"
)

dependencies {
    implementation(project(":common"))
    shadow(project(":common"))

    deps.forEach {
        implementation(it)
        shadow(it)
    }
}

tasks {
    build {
        dependsOn("shadowJar")
    }

    withType<ShadowJar> {
        mergeServiceFiles()
        configurations = listOf(project.configurations.shadow.get())
        archiveFileName.set("AudioServerStandalone.jar")

        manifest {
            attributes["Main-Class"] = "net.blockventuremc.audioserver.StartKt"
        }
    }
}