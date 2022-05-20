plugins {
    kotlin("jvm") version "1.6.20"
    java
    application
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("com.google.cloud.tools.jib") version "3.2.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
}

group = "org.example"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib"))
    implementation(platform("org.http4k:http4k-bom:4.25.8.0"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-server-undertow")
    implementation("org.http4k:http4k-format-gson")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation(platform("io.arrow-kt:arrow-stack:1.0.0"))
    implementation("io.arrow-kt:arrow-core")
    implementation("io.arrow-kt:arrow-fx-coroutines")
    implementation("io.arrow-kt:arrow-fx-stm")
    implementation("org.apache.kafka:kafka-clients:3.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

if (project.hasProperty("pixelArt")) {
    project.setProperty("mainClassName", "PixelArtKt")
} else {
    project.setProperty("mainClassName", "BotKt")
}



jib {
    from {
        image = "openjdk:17-slim-buster"
    }
    to {
        image = "fteychene/pixelcanvas-bot"
        tags = setOf(project.version as String, "latest")
    }
    container {
        mainClass = "BotKt"
        ports = listOf("8080")
        format = com.google.cloud.tools.jib.api.buildplan.ImageFormat.OCI
        jvmFlags = listOf(
            "-XX:+UseContainerSupport",
            "-XX:MinRAMPercentage=50",
            "-XX:MaxRAMPercentage=80"
        )
    }
}

