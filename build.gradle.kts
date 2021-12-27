plugins {
    kotlin("jvm") version "1.6.10"
}

group = "fer.hmo"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.bio:viktor:1.2.0")
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("org.slf4j:slf4j-simple:1.7.32")
    implementation("io.github.guyko:WeightedLottery:1.0.2")
    implementation("com.sksamuel.hoplite:hoplite-core:1.4.16")
    implementation("com.sksamuel.hoplite:hoplite-yaml:1.4.16")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}