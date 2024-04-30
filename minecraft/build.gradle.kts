plugins {
    kotlin("jvm")
}

group = "de.themeparkcraft.audioserver.minecraft"
version = "1.0-SNAPSHOT"

val minecraftVersion: String by project

dependencies {
    implementation(project(":common"))

    compileOnly("io.papermc.paper:paper-api:$minecraftVersion")
}
