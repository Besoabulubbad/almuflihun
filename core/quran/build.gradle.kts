import java.security.MessageDigest

plugins {
    id("almuflihun.kmp.library")
}

// Every Quran source file is byte-exact and listed in SOURCES.md with its checksum (ADR 0004). Fails the build if a
// file is missing, changed, or committed without a line in SOURCES.md.
val verifyQuranSources = tasks.register("verifyQuranSources") {
    group = "verification"
    description = "Checks core/quran/sources against the SHA-256 checksums in SOURCES.md."
    val sources = layout.projectDirectory.dir("sources")
    inputs.dir(sources)
    val marker = layout.buildDirectory.file("verifyQuranSources/ok")
    outputs.file(marker)
    doLast {
        val dir = sources.asFile
        val listed = Regex("""^\| `([^`]+)` \|.*\| `([0-9a-f]{64})` \|$""", RegexOption.MULTILINE)
            .findAll(dir.resolve("SOURCES.md").readText())
            .associate { it.groupValues[1] to it.groupValues[2] }
        val present = dir.listFiles().orEmpty().map { it.name }.filter { it != "SOURCES.md" }.toSet()
        val problems = buildList {
            (present - listed.keys).forEach { add("$it is not listed in SOURCES.md") }
            listed.forEach { (name, expected) ->
                val file = dir.resolve(name)
                if (!file.exists()) {
                    add("$name is listed but missing")
                } else {
                    val actual = MessageDigest.getInstance("SHA-256").digest(file.readBytes())
                        .joinToString("") { "%02x".format(it) }
                    if (actual != expected) add("$name changed: expected $expected, found $actual")
                }
            }
        }
        check(problems.isEmpty()) { "Quran sources do not match SOURCES.md:\n" + problems.joinToString("\n") }
        marker.get().asFile.apply { parentFile.mkdirs() }.writeText("ok")
    }
}

tasks.named("check") { dependsOn(verifyQuranSources) }
