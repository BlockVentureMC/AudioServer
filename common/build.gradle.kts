plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.0.0-RC2"
    id("maven-publish")
}

val protobufVersion: String by project
val coroutinesVersion: String by project

dependencies {
    testImplementation(kotlin("test"))

    api("com.rabbitmq:amqp-client:5.21.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:$protobufVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
}

publishing {
    repositories {
        mavenLocal()
        maven {
            name = "Nexus"
            url = uri("https://nexus.flawcra.cc/repository/maven-blockventure/")
            credentials {
                username = project.findProperty("nexus.user") as String? ?: System.getenv("BLOCKVENTURE_MVNUSER")
                password = project.findProperty("nexus.key") as String? ?: System.getenv("BLOCKVENTURE_MVNPASS")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
        }
    }
}