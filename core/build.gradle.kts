group = "io.validy"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

dependencies {
    // validy-core has zero runtime dependencies — pure Java 24, nothing else.
    // Add test dependencies only.
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}