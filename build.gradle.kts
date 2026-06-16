plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    application
}

group = "edu.dyds"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    implementation("io.ktor:ktor-client-core-jvm:2.3.12")
    implementation("io.ktor:ktor-client-cio-jvm:2.3.12")
    implementation("io.ktor:ktor-client-content-negotiation-jvm:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.12")
    implementation("io.ktor:ktor-client-logging-jvm:2.3.12")
    implementation("org.slf4j:slf4j-simple:2.0.13")

    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("io.ktor:ktor-client-mock-jvm:2.3.12")
}

kotlin {
    jvmToolchain(21)
}

sourceSets {
    named("main") {
        kotlin.srcDir("composeApp/src/commonMain/kotlin")
        kotlin.srcDir("composeApp/src/desktopMain/kotlin")
    }
    named("test") {
        kotlin.srcDir("composeApp/src/desktopTest/kotlin")
    }
}

application {
    mainClass.set("edu.dyds.trips.MainKt")
}

tasks.test {
    useJUnit()
}


