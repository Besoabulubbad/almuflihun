// Root build: plugins are declared here (apply false) so every module resolves the same versions, and
// repository-wide tooling (dependency analysis, the build-logic test suite) is configured once.
plugins {
    base
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.dependencyAnalysis)
}

tasks.check {
    dependsOn(gradle.includedBuild("build-logic").task(":convention:test"))
}

// `./gradlew buildHealth`. Applied per module by the convention plugins; the root aggregates. Every category fails.
dependencyAnalysis {
    structure {
        // Compose Multiplatform: we declare org.jetbrains.compose.* coordinates, which resolve to androidx.compose.*
        // on Android. One bundle so the analyser does not ask for the androidx twins.
        bundle("compose-multiplatform") {
            listOf("runtime", "foundation", "ui", "material3", "animation", "components").forEach {
                includeGroup("org.jetbrains.compose.$it")
            }
            listOf("runtime", "foundation", "ui", "material3", "animation").forEach {
                includeGroup("androidx.compose.$it")
            }
        }
    }
    issues {
        all {
            onAny { severity("warn") }
            onUnusedDependencies { severity("fail") }
            onUsedTransitiveDependencies { severity("fail") }
            onIncorrectConfiguration { severity("fail") }
            onCompileOnly { severity("fail") }
            onRuntimeOnly { severity("fail") }
            onUnusedAnnotationProcessors { severity("fail") }
            onRedundantPlugins { severity("fail") }
        }
    }
}
