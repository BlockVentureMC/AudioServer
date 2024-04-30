plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.0.0-RC2"
}

group = "de.themeparkcraft.audioserver.common"
version = "1.0-SNAPSHOT"


dependencies {
    testImplementation(kotlin("test"))

    api("com.rabbitmq:amqp-client:5.21.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.6.3")
}

tasks.test {
    useJUnitPlatform()
}