plugins {
    id("java")
}

// ── Shared configuration applied to every subproject ─────────────────────────
subprojects {

    apply(plugin = "java-library")

    group   = "io.valify"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }

    // Shared compiler settings
    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(24)
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(listOf(
            "--enable-preview",          // needed for sealed types + pattern matching in Java 21+
            "-Xlint:all",
            "-Xlint:-processing"         // suppress annotation processor warnings
        ))
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        jvmArgs("--enable-preview")
    }

    tasks.withType<JavaExec>().configureEach {
        jvmArgs("--enable-preview")
    }
}