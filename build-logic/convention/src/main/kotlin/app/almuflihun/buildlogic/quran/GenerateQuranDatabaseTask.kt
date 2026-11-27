package app.almuflihun.buildlogic.quran

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/** Writes `files/quran.db` under [outputDirectory], a Compose Resources directory (ADR 0015). */
@CacheableTask
abstract class GenerateQuranDatabaseTask : DefaultTask() {

    @get:InputFile @get:PathSensitive(PathSensitivity.NONE)
    abstract val tanzil: RegularFileProperty

    @get:InputFile @get:PathSensitive(PathSensitivity.NONE)
    abstract val layout: RegularFileProperty

    @get:InputFile @get:PathSensitive(PathSensitivity.NONE)
    abstract val glyphWords: RegularFileProperty

    @get:InputFile @get:PathSensitive(PathSensitivity.NONE)
    abstract val textWords: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val out = outputDirectory.get().asFile
        out.deleteRecursively()
        QuranDatabaseBuilder.build(
            QuranDatabaseBuilder.Sources(
                tanzil = tanzil.get().asFile,
                layout = layout.get().asFile,
                glyphWords = glyphWords.get().asFile,
                textWords = textWords.get().asFile,
            ),
            out.resolve("files/quran.db"),
        )
    }
}
