plugins {
    kotlin("jvm")
    id("maven-publish")
}

group = "de.themeparkcraft.audioserver.minecraft"
version = "0.1-SNAPSHOT"

val minecraftVersion: String by project

dependencies {
    implementation(project(":common"))

    compileOnly("io.papermc.paper:paper-api:$minecraftVersion")
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