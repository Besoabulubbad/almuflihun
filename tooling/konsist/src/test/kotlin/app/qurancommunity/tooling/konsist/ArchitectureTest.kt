package app.qurancommunity.tooling.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

/**
 * The module contract (docs/architecture/modules.md) and the forbidden-import rules from CLAUDE.md, asserted over
 * every Kotlin file of the product (ADR 0011). Written so that skeleton modules pass vacuously and the first
 * violating file fails.
 */
class ArchitectureTest {

    /** Product code only (paths from the repository root): the build logic and this tooling are not shipped. */
    private val scope = Konsist.scopeFromProject()
        .slice { file -> PRODUCT_ROOTS.any { file.projectPath.startsWith(it) } }

    private val featureFiles: List<KoFileDeclaration> = scope.files.filter { it.path.contains("/feature/") }

    @Test
    fun `every file's package matches its module`() {
        scope.files
            .filter { it.path.contains("/src/") }
            // Vendored upstream code keeps its own package so updates stay a clean copy (ADR 0017).
            .filterNot { it.path.contains(VENDORED_ASTRONOMY_ENGINE) }
            .assertTrue(additionalMessage = "Packages follow app.qurancommunity.<layer>.<module> (ADR 0009).") { file ->
                val module = file.projectPath.removePrefix("/").substringBefore("/src/")
                val expected = "app.qurancommunity." + module.split('/').joinToString(".") { it.replace("-", "") }
                val pkg = file.packagee?.name.orEmpty()
                file.projectPath.startsWith("/app/android/") || pkg == expected || pkg.startsWith("$expected.")
            }
    }

    @Test
    fun `feature modules never import infrastructure or engines directly`() {
        featureFiles.assertFalse(
            additionalMessage = "Features reach data, Firebase and the engines only through core:domain use cases.",
        ) { file ->
            file.imports.any { import -> INFRASTRUCTURE.any { import.name.startsWith(it) } }
        }
    }

    @Test
    fun `feature modules never import another feature`() {
        featureFiles.assertFalse(
            additionalMessage = "Features talk through navigation keys and shared use cases, never directly.",
        ) { file ->
            val own = "$FEATURE_PACKAGE.${ownFeature(file.path).replace("-", "")}."
            file.imports.any { it.name.startsWith("$FEATURE_PACKAGE.") && !it.name.startsWith(own) }
        }
    }

    @Test
    fun `material is imported only inside core designsystem`() {
        scope.files
            .filter { !it.path.contains("/core/designsystem/") }
            .assertFalse(
                additionalMessage = "Material 3 lives in core:designsystem; compose the design-system components.",
            ) { file ->
                file.imports.any { MATERIAL_PACKAGES.any { prefix -> it.name.startsWith(prefix) } }
            }
    }

    @Test
    fun `firebase is touched only by core firebase, core analytics and the app shells`() {
        scope.files
            .filterNot { file -> FIREBASE_OWNERS.any { file.path.contains(it) } }
            .assertFalse(additionalMessage = "Firebase stays behind its seams (ADR 0005, ADR 0008).") { file ->
                file.imports.any { import -> FIREBASE_PACKAGES.any { import.name.startsWith(it) } }
            }
    }

    @Test
    fun `analytics events are sent only through core analytics`() {
        scope.files
            .filter { !it.path.contains("/core/analytics/") }
            .assertFalse(
                additionalMessage = "Only the allow-listed wrapper may log events (ADR 0008, tracking plan).",
            ) { file ->
                file.imports.any { import -> ANALYTICS_SDKS.any { import.name.startsWith(it) } }
            }
    }

    @Test
    fun `navigation keys are declared only in core navigation`() {
        scope.files
            .filter { !it.path.contains("/core/navigation/") }
            .assertFalse(additionalMessage = "NavKeys belong in core:navigation only.") { file ->
                val declarations = file.classes() + file.interfaces() + file.objects()
                declarations.any { it.hasParentWithName("NavKey", indirectParents = true) }
            }
    }

    @Test
    fun `every navigation key is registered for saving and restoring`() {
        val navigation = scope.files.filter { it.path.contains("/core/navigation/src/commonMain/") }
        val keys = navigation.flatMap { it.classes() + it.objects() }
            .filter { it.hasParentWithName("AppNavKey") }
            .map { it.name }
        val registrations = navigation.joinToString("\n") { it.text }
        val missing = keys.filterNot { "subclass($it::class" in registrations }
        check(missing.isEmpty()) { "Navigation keys missing from appNavSerializersModule: $missing" }
    }

    @Test
    fun `koin component service locator is used only by the app shell`() {
        scope.files
            .filter { !it.path.contains("/app/shared/") }
            .assertFalse(
                additionalMessage = "Constructor injection only; KoinComponent is reserved for app:shared.",
            ) { file ->
                file.imports.any { it.name == "org.koin.core.component.KoinComponent" }
            }
    }

    @Test
    fun `platform locale apis are touched only by core localization`() {
        scope.files
            .filter { !it.path.contains("/core/localization/") }
            .filterNot { TEST_SOURCE_SETS.any { set -> it.path.contains("/src/$set/") } }
            .assertFalse(
                additionalMessage = "core:localization is the single source for Arabic formatting and direction.",
            ) { file ->
                file.imports.any { import -> PLATFORM_LOCALE_APIS.any { import.name.startsWith(it) } }
            }
    }

    @Test
    fun `feature view models extend ScreenViewModel`() {
        val viewModels = featureFiles.flatMap { it.classes() }.filter { it.name.endsWith("ViewModel") }
        if (viewModels.isEmpty()) return // skeleton modules
        viewModels.assertTrue(additionalMessage = "Every screen follows MVI through ScreenViewModel.") {
            it.hasParent(indirectParents = true) { parent -> parent.name.substringBefore("<") == "ScreenViewModel" }
        }
    }

    @Test
    fun `a feature with a view model also exposes Route and Screen composables`() {
        featureFiles
            .groupBy { ownFeature(it.path) }
            .filterValues { files -> files.any { f -> f.classes().any { it.name.endsWith("ViewModel") } } }
            .forEach { (feature, files) ->
                val functionNames = files.flatMap { it.functions() }.map { it.name }
                check(functionNames.any { it.endsWith("Route") }) { "feature:$feature has no *Route composable" }
                check(functionNames.any { it.endsWith("Screen") }) { "feature:$feature has no *Screen composable" }
            }
    }

    private fun ownFeature(path: String): String = path.substringAfter("/feature/").substringBefore("/")

    private companion object {
        const val FEATURE_PACKAGE = "app.qurancommunity.feature"
        val PRODUCT_ROOTS = listOf("/app/", "/core/", "/feature/")
        val TEST_SOURCE_SETS = listOf("commonTest", "androidHostTest", "androidDeviceTest", "iosTest")
        val INFRASTRUCTURE = listOf(
            "app.qurancommunity.core.data.",
            "app.qurancommunity.core.database.",
            "app.qurancommunity.core.firebase.",
            "app.qurancommunity.core.quran.",
            "app.qurancommunity.core.astronomy.",
            "app.qurancommunity.core.audio.",
            "app.qurancommunity.core.update.",
        )
        val MATERIAL_PACKAGES = listOf("androidx.compose.material3", "androidx.compose.material.")
        val FIREBASE_OWNERS = listOf("/core/firebase/", "/core/analytics/", "/app/shared/", "/app/android/")
        val FIREBASE_PACKAGES = listOf("dev.gitlive.firebase", "com.google.firebase")
        val ANALYTICS_SDKS = listOf(
            "dev.gitlive.firebase.analytics",
            "dev.gitlive.firebase.crashlytics",
            "com.google.firebase.analytics",
            "com.google.firebase.crashlytics",
        )
        val PLATFORM_LOCALE_APIS = listOf(
            "java.util.Locale",
            "java.text.",
            "platform.Foundation.NSLocale",
            "android.icu.",
        )
    }
}

/** Astronomy Engine, vendored as upstream wrote it (ADR 0017). */
private const val VENDORED_ASTRONOMY_ENGINE = "/io/github/cosinekitty/astronomy/"
