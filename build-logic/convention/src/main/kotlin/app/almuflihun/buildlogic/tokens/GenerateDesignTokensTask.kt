package app.almuflihun.buildlogic.tokens

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/** Generates the design-token Kotlin sources from `design/tokens.json` (see [DesignTokenCodegen]). */
@CacheableTask
abstract class GenerateDesignTokensTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val tokensFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val packageDir = outputDirectory.get().asFile.resolve(DesignTokenCodegen.PACKAGE.replace('.', '/'))
        packageDir.deleteRecursively()
        packageDir.mkdirs()
        DesignTokenCodegen.generate(tokensFile.get().asFile.readText()).forEach { file ->
            packageDir.resolve(file.name).writeText(file.content)
        }
    }
}
