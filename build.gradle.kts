import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.41"
}

group = "ru.falseteam.syncdns"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("me.legrange:mikrotik:3.0.5")
    implementation("org.ini4j:ini4j:0.5.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks { withType<Jar> {
    manifest { attributes["Main-Class"] = "${project.group}.MainKt" }
    configurations["compileClasspath"].forEach { from(zipTree(it.absoluteFile)) }
} }