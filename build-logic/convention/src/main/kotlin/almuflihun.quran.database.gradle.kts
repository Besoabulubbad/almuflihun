// Builds the bundled read-only Quran database from core/quran/sources (ADR 0015) into Compose Resources, so the app
// reads it as `files/quran.db` on Android and iOS. Applied by core:quran only. The output lives under build/.
import app.almuflihun.buildlogic.quran.GenerateQuranDatabaseTask

plugins {
    id("org.jetbrains.compose")
    // Required with the Compose plugin (Compose Multiplatform 1.6.10+); core:quran itself declares no composables.
    id("org.jetbrains.kotlin.plugin.compose")
}

val generateQuranDatabase = tasks.register("generateQuranDatabase", GenerateQuranDatabaseTask::class) {
    group = "build"
    description = "Builds the bundled Quran database from core/quran/sources."
    val sources = project.layout.projectDirectory.dir("sources")
    tanzil.set(sources.file("quran-simple.txt"))
    layout.set(sources.file("qpc-v2-15-lines.db"))
    glyphWords.set(sources.file("qpc-v2.db"))
    textWords.set(sources.file("digital-khatt-v2.db"))
    outputDirectory.set(project.layout.buildDirectory.dir("generated/quran/composeResources"))
}

compose.resources {
    customDirectory(
        sourceSetName = "commonMain",
        directoryProvider = generateQuranDatabase.flatMap { it.outputDirectory },
    )
}
