plugins {
    kotlin("jvm")
    id("maven-publish")
}

val minecraftVersion: String by project

dependencies {
    implementation(project(":common"))

    compileOnly("io.papermc.paper:paper-api:$minecraftVersion")
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