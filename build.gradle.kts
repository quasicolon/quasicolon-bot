plugins {
    kotlin("jvm") version "1.9.21"
    application
    id("io.freefair.lombok") version "8.4"
}

group = "dev.qixils.quasicolon"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("dev.qixils.quasicolon:quasicord:1.0.0-SNAPSHOT")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.9.0-RC")
    implementation("club.minnced:jda-ktx:0.11.0-beta.20")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.2")
    implementation("ch.qos.logback:logback-classic:1.5.6")
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