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
    implementation("org.apache.pinot:pinot-java-client:0.10.0")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("org.apache.logging.log4j:log4j-slf4j18-impl:2.17.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

project.setProperty("mainClassName", "request.MainKt")