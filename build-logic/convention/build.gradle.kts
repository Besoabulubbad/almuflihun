plugins {
    `kotlin-dsl`
}

group = "app.qurancommunity.buildlogic"

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
    implementation(libs.roborazzi.gradlePlugin)

    // design/tokens.json → Kotlin (qurancommunity.tokens, ADR 0001). Parsed as a JsonElement tree, so no serialization
    // compiler plugin is needed inside the build logic.
    implementation(libs.kotlinx.serialization.json)

    // core/quran/sources → the bundled quran.db (qurancommunity.quran.database, ADR 0015). Build time only, never in the app.
    implementation(libs.sqlite.jdbc)

    testImplementation(libs.kotlin.test)
}

tasks.test {
    // The generator test runs against the real token file, so the build logic is proven on every `check`.
    systemProperty("qurancommunity.tokensFile", rootDir.parentFile.resolve("design/archive/v5/tokens.json").absolutePath)
    systemProperty("qurancommunity.quranSources", rootDir.parentFile.resolve("core/quran/sources").absolutePath)
}
