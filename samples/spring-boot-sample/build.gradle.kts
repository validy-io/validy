plugins {
    id("java")
    id("org.springframework.boot") version "4.0.0"
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

dependencies {
    // ── Valify ────────────────────────────────────────────────────────────────
    implementation(project(":core"))
    implementation(project(":spring"))

    // ── Spring Boot ───────────────────────────────────────────────────────────
    implementation("org.springframework.boot:spring-boot-starter-web")

    // ── Test ──────────────────────────────────────────────────────────────────
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
}