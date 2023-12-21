plugins {
    kotlin("jvm") version "1.9.21"
    application
}

group = "dev.qixils.quasicolon"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
    mavenLocal()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("dev.qixils.quasicolon:quasicord:1.0.0-SNAPSHOT")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC")
    implementation("com.github.minndevelopment:jda-ktx:9370cb13cc64646862e6f885959d67eb4b157e4a")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.2")
    implementation("ch.qos.logback:logback-classic:1.4.14")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("dev.qixils.quasicolon.bot.QuasicolonKt")
}