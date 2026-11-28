package app.qurancommunity.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

/** The shared version catalog (`gradle/libs.versions.toml`), for use inside precompiled script plugins. */
val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun VersionCatalog.version(alias: String): String = findVersion(alias).get().requiredVersion

fun VersionCatalog.library(alias: String): Provider<MinimalExternalModuleDependency> = findLibrary(alias).get()

/** `:core:quran` → `app.qurancommunity.core.quran`; `:feature:quran-hub` → `app.qurancommunity.feature.quranhub` (ADR 0021). */
val Project.qurancommunityNamespace: String
    get() = "app.qurancommunity" + path.split(':').filter { it.isNotEmpty() }
        .joinToString("") { "." + it.replace("-", "") }
