// Generates the design tokens from design/tokens.json into commonMain (ADR 0001). Applied by core:designsystem only.
// The generated files live under build/ and are never committed or edited by hand.
import app.almuflihun.buildlogic.tokens.GenerateDesignTokensTask

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

val generateDesignTokens by tasks.registering(GenerateDesignTokensTask::class) {
    group = "build"
    description = "Generates Kotlin design tokens from design/tokens.json."
    tokensFile.set(rootProject.layout.projectDirectory.file("design/tokens.json"))
    outputDirectory.set(layout.buildDirectory.dir("generated/tokens/commonMain/kotlin"))
}

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(generateDesignTokens.map { it.outputDirectory })
        }
    }
}
