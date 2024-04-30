plugins {
    kotlin("jvm") version "2.0.0-RC2"
}


val audioServerVersion: String by project

group = "de.themeparkcraft.audioserver"
version = audioServerVersion

repositories {
    maven("https://nexus.flawcra.cc/repository/maven-mirrors/")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        freeCompilerArgs.addAll(listOf("-opt-in=kotlin.RequiresOptIn", "-Xopt-in=dev.kord.common.annotation.KordPreview", "-Xopt-in=dev.kord.common.annotation.KordExperimental", "-Xopt-in=kotlin.time.ExperimentalTime", "-Xopt-in=kotlin.contracts.ExperimentalContracts"))
    }
}

subprojects {

    repositories {
        maven("https://nexus.flawcra.cc/repository/maven-mirrors/")
    }

    group = "de.themeparkcraft.audioserver"
    version = audioServerVersion

}