plugins {
    `java-library`
}

java {
    toolchain {
        // Spring Boot 4 minimum is 17; bump to 24 to match the rest of your project
        languageVersion = JavaLanguageVersion.of(24)
    }
}

repositories {
    mavenCentral()
}

dependencies {

    // ── BOM — import via platform() so all Spring + Jakarta versions are aligned
    // This replaces Maven's <dependencyManagement> + BOM import pattern.
    // No versions needed on any spring-* or jakarta-* dependency below.
    api(platform("org.springframework.boot:spring-boot-dependencies:4.0.0"))

    // ── Valify core ───────────────────────────────────────────────────────────
    api(project(":core"))

    // ── Spring Boot autoconfigure ─────────────────────────────────────────────
    // `compileOnly` = available when we compile, but NOT added to the consumer's
    // runtime classpath. The application already has these via spring-boot-starter-web.
    // This is the Gradle equivalent of Maven's <optional>true</optional>.
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    compileOnly("org.springframework.boot:spring-boot")
    compileOnly("org.springframework:spring-webmvc")

    // ── Jakarta Validation 3.1 (EE 11 baseline shipped with Boot 4) ──────────
    // Optional — only needed if you want Bean Validation interop alongside Valify.
    compileOnly("jakarta.validation:jakarta.validation-api")

    // ── Tests ─────────────────────────────────────────────────────────────────
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
}