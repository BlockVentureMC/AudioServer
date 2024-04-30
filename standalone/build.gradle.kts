import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "de.themeparkcraft.audioserver"
version = "1.0-SNAPSHOT"


dependencies {
    implementation(project(":common"))
    shadow(project(":common"))
}

tasks {
    build {
        dependsOn("shadowJar")
    }

    withType<ShadowJar> {
        mergeServiceFiles()
        configurations = listOf(project.configurations.shadow.get())
        archiveFileName.set("AudioServerStandalone.jar")
    }
}