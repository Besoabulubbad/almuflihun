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
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.dependencyAnalysis)
}

tasks.check {
    dependsOn(gradle.includedBuild("build-logic").task(":convention:test"))
}

// The analyser's per-module tasks are never taken from the build cache: their cache key misses a change of package
// (the rename to app.qurancommunity, ADR 0021, restored summaries naming the old classes and buildHealth reported
// every module as unused). They are quick to recompute.
subprojects {
    tasks.configureEach {
        if (javaClass.name.startsWith("com.autonomousapps.")) {
            outputs.doNotCacheIf("dependency-analysis cache keys miss package moves") { true }
        }
    }
}

// `./gradlew buildHealth`. Applied per module by the convention plugins; the root aggregates. Every category fails.
// Modules that are still skeletons warn instead; remove a module from this list in the PR that gives it code.
val skeletonModules = (
    listOf("audio",
        "firebase", "update").map { ":core:$it" } +
        listOf("home", "moon", "quran-hub", "mushaf", "hizb", "doors", "diyaa", "collections", "live", "community", "profile",
            "admin", "settings").map { ":feature:$it" }
)
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
        // The JetBrains multiplatform forks of AndroidX resolve to Google's artifacts on Android (as in Sojourn).
        bundle("lifecycle") {
            includeGroup("org.jetbrains.androidx.lifecycle")
            includeGroup("androidx.lifecycle")
        }
        bundle("savedstate") {
            includeGroup("org.jetbrains.androidx.savedstate")
            includeGroup("androidx.savedstate")
        }
        bundle("navigationevent") {
            includeGroup("org.jetbrains.androidx.navigationevent")
            includeGroup("androidx.navigationevent")
        }
        bundle("navigation3") {
            includeGroup("org.jetbrains.androidx.navigation3")
            includeGroup("androidx.navigation3")
        }
        // koinViewModel() is inline, so its bytecode points at koin-core-viewmodel.
        bundle("koin-viewmodel") {
            includeDependency("io.insert-koin:koin-compose-viewmodel")
            includeDependency("io.insert-koin:koin-core-viewmodel")
        }
    }
    issues {
        all {
            onAny { severity("warn") }
            onUnusedDependencies {
                severity("fail")
                // qurancommunity.kmp.library gives every module kotlin-test, ready for its first test.
                exclude("org.jetbrains.kotlin:kotlin-test")
            }
            onUsedTransitiveDependencies {
                severity("fail")
                // The JUnit bridge and reflection come with the test runtime, not with our code (as in Sojourn).
                exclude("org.jetbrains.kotlin:kotlin-test-junit", "org.jetbrains.kotlin:kotlin-reflect")
            }
            onIncorrectConfiguration { severity("fail") }
            onCompileOnly { severity("fail") }
            onRuntimeOnly { severity("fail") }
            onUnusedAnnotationProcessors { severity("fail") }
            onRedundantPlugins { severity("fail") }
        }
        skeletonModules.forEach { path ->
            project(path) {
                onAny { severity("warn") }
                onUnusedDependencies { severity("warn") }
                onUsedTransitiveDependencies { severity("warn") }
                onIncorrectConfiguration { severity("warn") }
                onCompileOnly { severity("warn") }
                onRuntimeOnly { severity("warn") }
            }
        }
    }
}
