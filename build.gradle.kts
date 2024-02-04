import com.adarshr.gradle.testlogger.theme.ThemeType

plugins {
    kotlin("jvm") version "1.9.21"
    id("com.adarshr.test-logger") version "4.0.0"
}

group = "com.github.dimanyfantakis"
version = "1.0"

val kotestVersion = "5.8.0"
val mockkVersion  = "1.13.8"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("io.ktor:ktor-network:2.3.7")
    implementation("io.ktor:ktor-network-tls:2.3.7")

    // Logging.
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")
    implementation("ch.qos.logback:logback-classic:1.4.12")

    // Tests.
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

tasks.test {
    useJUnitPlatform()

    // Log the report.
    testlogger {
        theme                      = ThemeType.MOCHA
        showExceptions             = true
        showStackTraces            = true
        showFullStackTraces        = false
        showCauses                 = true
        slowThreshold              = 2000
        showSummary                = true
        showSimpleNames            = false
        showPassed                 = true
        showSkipped                = true
        showFailed                 = true
        showOnlySlow               = false
        showStandardStreams        = false
        showPassedStandardStreams  = true
        showSkippedStandardStreams = true
        showFailedStandardStreams  = true
        logLevel                   = LogLevel.LIFECYCLE
    }
}

kotlin {
    jvmToolchain(17)
}