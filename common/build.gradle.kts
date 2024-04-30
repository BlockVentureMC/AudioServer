plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.0.0-RC2"
    id("maven-publish")
}

group = "de.themeparkcraft.audioserver"
version = "1.0-SNAPSHOT"

val protobufVersion: String by project

dependencies {
    testImplementation(kotlin("test"))

    api("com.rabbitmq:amqp-client:5.21.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:$protobufVersion")
}

publishing {
    repositories {
        mavenLocal()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/BlockVentureMC/AudioServer")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
        }
    }
}