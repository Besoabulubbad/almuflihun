// Generates the design tokens into commonMain (ADR 0001). Applied by core:designsystem only. Reads the v5 tokens
// (design/archive/v5/tokens.json) until Phase 5 rebuilds the design system on v7's (design/tokens.json, ADR 0018).
// The generated files live under build/ and are never committed or edited by hand.
import app.qurancommunity.buildlogic.tokens.GenerateDesignTokensTask

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

val generateDesignTokens by tasks.registering(GenerateDesignTokensTask::class) {
    group = "build"
    description = "Generates Kotlin design tokens from design/tokens.json."
    tokensFile.set(rootProject.layout.projectDirectory.file("design/archive/v5/tokens.json"))
    outputDirectory.set(layout.buildDirectory.dir("generated/tokens/commonMain/kotlin"))
}

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(generateDesignTokens.map { it.outputDirectory })
        }
    }
}
