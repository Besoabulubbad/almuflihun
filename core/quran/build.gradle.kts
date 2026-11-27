import java.security.MessageDigest

plugins {
    id("almuflihun.kmp.library")
    id("almuflihun.koin")
    id("org.jetbrains.kotlin.plugin.serialization")
    // Builds files/quran.db from sources/ into Compose Resources (ADR 0015).
    id("almuflihun.quran.database")
    id("app.cash.sqldelight")
}

kotlin {
    compilerOptions {
        // PlatformTest is an expect class: the tests share one Robolectric runner on Android.
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    android {
        androidResources {
            // Compose Resources ship as Android assets.
            enable = true
        }
    }
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            api(libs.koin.core) // quranModule is public
            api(project(":core:common"))
            // Reads the bundled quran.db on Android and iOS. Resources only: core:quran has no UI.
            implementation(libs.compose.components.resources)
            // The Compose compiler, required by the Compose plugin, needs the runtime on the classpath.
            implementation(libs.compose.runtime)
            implementation(libs.sqldelight.runtime)
            api(libs.kotlinx.coroutines.core) // PageFontDownloader.progress is a StateFlow
            implementation(libs.kotlinx.serialization.json) // reads design/data
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.androidDriver)
            implementation(libs.androidx.sqlite) // the driver's SupportSQLite types
            implementation(libs.kotlinx.serialization.core) // the generated serializers' runtime types
            api(libs.androidx.work.runtime) // FontWorkerFactory is a WorkerFactory (ADR 0016)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.nativeDriver)
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
            implementation(libs.kotlinx.serialization.json) // reads design/data in ContentPlacementTest
            implementation(libs.kotlinx.coroutines.test) // runAppTest's TestScope
        }
        named("androidHostTest").dependencies {
            implementation(libs.robolectric)
            implementation(libs.robolectric.annotations)
            implementation(libs.robolectric.shadowsFramework) // RuntimeEnvironment
            implementation(libs.androidx.work.testing)
            implementation(libs.junit)
            implementation(libs.kotlinx.coroutines.test)
        }
        named("androidDeviceTest").dependencies {
            implementation(libs.androidx.testExt.junit)
            implementation(libs.androidx.testMonitor)
            runtimeOnly(libs.androidx.testRunner) // AndroidJUnitRunner
            implementation(libs.junit)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

compose.resources {
    packageOfResClass = "app.almuflihun.core.quran.resources"
}

// The bundled database is read-only: SQLDelight only types the queries; there are no migrations (ADR 0015).
sqldelight {
    databases {
        create("QuranDatabase") {
            packageName.set("app.almuflihun.core.quran.db")
        }
    }
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

// QpcCorpusTest checks the real page fonts when QPC_WOFF_DIR names a folder of them (never committed, ADR 0013).
val qpcWoffDir = providers.environmentVariable("QPC_WOFF_DIR").orElse("")
tasks.withType<Test>().configureEach {
    inputs.property("qpcWoffDir", qpcWoffDir)
    environment("QPC_WOFF_DIR", qpcWoffDir.get())
}
