plugins {
    kotlin("jvm") version "2.0.21" // Matches your Android app's Kotlin version
    kotlin("plugin.serialization") version "2.0.21"
    application
}

application {
    // 🎯 Ensure this matches your exact backend package path + file name + "Kt"
    mainClass.set("com.example.flexshare.backend.ApplicationKt")
}
group = "com.example.flexshare"
version = "1.0-SNAPSHOT"


// 🚀 Simple, explicit task registration that never fails to sync
tasks.register<JavaExec>("runServer") {
    group = "application"
    description = "Runs the Ktor backend server"

    mainClass.set("com.example.flexshare.backend.ApplicationKt")
    classpath = sourceSets["main"].runtimeClasspath
}



val ktorVersion = "2.3.12"

dependencies {
    // 📡 Ktor Core & Core Netty Engine Engine
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")

    // 🪵 Logging and JSON Formatting Serialization
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")

    // 🪵 Readable terminal logging print utility
    implementation("ch.qos.logback:logback-classic:1.4.14")

    implementation("org.jetbrains.exposed:exposed-core:0.50.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.50.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.50.1")
    implementation("com.h2database:h2:2.2.224")
}