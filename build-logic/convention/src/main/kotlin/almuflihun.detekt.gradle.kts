// detekt: the single linter (Sojourn ADR 0014, adopted by ADR 0011). Project config in config/detekt/detekt.yml on top of the defaults,
// formatting rules and the Compose rule set. Custom design-system rules join via tooling/detekt-rules.
import app.almuflihun.buildlogic.libs
import app.almuflihun.buildlogic.library

plugins {
    id("io.gitlab.arturbosch.detekt")
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    // KMP layout: every source set under src/, not only src/main.
    source.setFrom(files("src"))
    parallel = true
    basePath = rootProject.projectDir.absolutePath
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    exclude("**/build/**", "**/generated/**")
    reports {
        html.required.set(true)
        sarif.required.set(true)
        xml.required.set(false)
        txt.required.set(false)
    }
}

dependencies {
    "detektPlugins"(libs.library("detekt-formatting"))
    "detektPlugins"(libs.library("detekt-composeRules"))
    // The design and Quran rules as lint. Loaded everywhere, including the rule module itself, so the `almuflihun`
    // config section validates in every run; each rule's includes keep it to the code it governs.
    "detektPlugins"(project(":tooling:detekt-rules"))
}
