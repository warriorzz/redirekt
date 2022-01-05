plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
    application
}

group = "io.github.warriorzz.redirekt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://schlaubi.jfrog.io/artifactory/envconf/")
    maven {
        url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
        name = "ktor-eap"
    }
    maven {
        url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
        name = "ktor-eap"
    }
}

dependencies {
    implementation(platform("io.ktor:ktor-bom:1.6.2"))
    implementation("io.ktor", "ktor-client-cio")
    implementation("io.ktor", "ktor-server-core")
    implementation("io.ktor", "ktor-server-cio")
    implementation("io.ktor", "ktor-freemarker")

    implementation("org.jetbrains:markdown:0.2.4")
    implementation("dev.schlaubi", "envconf", "1.1")
    implementation("org.slf4j", "slf4j-simple", "1.7.31")
    implementation("org.litote.kmongo", "kmongo-coroutine-serialization", "4.2.7")
}

application {
    mainClass.set("io.github.warriorzz.redirekt.LauncherKt")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}

ktlint {
    verbose.set(true)
    filter {
        disabledRules.add("no-wildcard-imports")
        disabledRules.add("no-multi-spaces")
        disabledRules.add("indent")

        exclude("**/build/**")
    }
}
