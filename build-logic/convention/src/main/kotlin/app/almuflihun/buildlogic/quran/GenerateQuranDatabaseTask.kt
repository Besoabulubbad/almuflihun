package app.almuflihun.buildlogic.quran

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Writes `files/quran.db` and `files/content/<name>.json` under [outputDirectory], a Compose Resources directory
 * (ADR 0015).
 */
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

    /** Tanzil's quran-data.js: surahs, juz and hizb quarters, sajdas. */
    @get:InputFile @get:PathSensitive(PathSensitivity.NONE)
    abstract val metadata: RegularFileProperty

    /** QUL's word roots, lemmas and stems. */
    @get:InputFile @get:PathSensitive(PathSensitivity.NONE)
    abstract val roots: RegularFileProperty

    @get:InputFile @get:PathSensitive(PathSensitivity.NONE)
    abstract val lemmas: RegularFileProperty

    @get:InputFile @get:PathSensitive(PathSensitivity.NONE)
    abstract val stems: RegularFileProperty

    /** `design/data`: the doors, slider, hizbs and الضياء, shipped unchanged as `files/content/<name>.json`. */
    @get:InputDirectory @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val content: DirectoryProperty

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
                metadata = metadata.get().asFile,
                roots = roots.get().asFile,
                lemmas = lemmas.get().asFile,
                stems = stems.get().asFile,
            ),
            out.resolve("files/quran.db"),
        )
        val contentOut = out.resolve("files/content").apply { mkdirs() }
        content.get().asFile.listFiles { f -> f.extension == "json" }.orEmpty().sortedBy { it.name }.forEach {
            it.copyTo(contentOut.resolve(it.name))
        }
    }
}
