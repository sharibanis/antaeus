import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	java
    base
    kotlin("jvm") version "1.3.21" apply false
    id("org.jmailen.kotlinter") version "1.20.1"
}

allprojects {
    group = "io.pleo"
    version = "1.0"

    repositories {
        mavenCentral()
        jcenter()
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.suppressWarnings = true
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

kotlinter {
    continuationIndentSize = 4
}

dependencies {
	implementation("io.github.microutils:kotlin-logging:1.5.9")
	implementation("org.slf4j:slf4j-api:1.7.28")
}
