import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java") // Java support
    alias(libs.plugins.kotlin) // Kotlin support
    alias(libs.plugins.shadow)
}

// Prevent that snapshot artifacts can be used for ktlint versions that have been released officially
repositories {
    mavenCentral()
    flatDir {
        dirs("libs")
    }
}
dependencies {
    // Until version 0.50.0, the "mu.Kotlin" logger was used. In 1.x version this has been replaced with
    // "io.github.oshai.kotlinlogging.KLogger".
    constraints {
        runtimeOnly(libs.slf4j.api) {
            because(
                "Transitive ktlint logging dependency (2.0.3) does not use the module classloader in ServiceLoader. Replace with newer SLF4J version",
            )
        }
        // ec4-core version 0.3.0 which is included in ktlint 0.50.0 fails on '.editorconfig' properties without value
        implementation("org.ec4j.core:ec4j-core:1.1.0") {
            because("Allows '.editorconfig' properties to be defined without any value")
        }
    }
    implementation("ktlint-ruleset-standard:0.43.2")
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        apiVersion.set(
            org
                .jetbrains
                .kotlin
                .gradle
                .dsl
                .KotlinVersion
                .KOTLIN_2_0,
        )
    }
}

tasks {
    withType<ShadowJar> {
        relocate(
            "com.pinterest.ktlint.logger",
            "com.pinterest.ktlint-0-50-0.logger",
        )
        relocate(
            "com.pinterest.ktlint.ruleset.standard",
            "com.pinterest.ktlint.ruleset.standard.V0_43_2",
        )

        minimize {
            exclude(dependency("ktlint-ruleset-standard:0.43.2"))
        }
    }
}
