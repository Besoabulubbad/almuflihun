plugins {
    `kotlin-dsl`
}

group = "app.almuflihun.buildlogic"

kotlin {
    jvmToolchain(21)
}

dependencies {
    // Plugin artifacts that the precompiled script plugins apply by id.
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.kotlin.composeCompilerGradlePlugin)
    implementation(libs.compose.gradlePlugin)
    implementation(libs.detekt.gradlePlugin)
    implementation(libs.dependencyAnalysis.gradlePlugin)

    // design/tokens.json → Kotlin (almuflihun.tokens, ADR 0001). Parsed as a JsonElement tree, so no serialization
    // compiler plugin is needed inside the build logic.
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.kotlin.test)
}

tasks.test {
    // The generator test runs against the real token file, so the build logic is proven on every `check`.
    systemProperty("almuflihun.tokensFile", rootDir.parentFile.resolve("design/tokens.json").absolutePath)
}
