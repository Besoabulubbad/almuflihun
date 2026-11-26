package app.almuflihun.tooling.detekt

import io.github.detekt.test.utils.compileContentForTest
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.lint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Each rule against a snippet that must fail and one that must pass. */
class AlmuflihunRulesTest {

    private fun Rule.findings(code: String, fileName: String = "FeatureScreen.kt") =
        lint(compileContentForTest(code.trimIndent(), fileName))

    @Test
    fun `TokensOnly flags raw colours and unit literals but not tokens`() {
        val rule = TokensOnly(Config.empty)
        val bad = rule.findings(
            """
            fun f() {
                val c = Color(0xFF2F4A7A)
                val p = 16.dp
                val s = 14.sp
                val n = -2.dp
            }
            """,
        )
        assertEquals(4, bad.size)
        val good = rule.findings(
            """
            fun f() {
                val c = AppTheme.colors.accent
                val p = Space.s16
                val q = p * 2
                val none = 0.dp
            }
            """,
        )
        assertTrue(good.isEmpty(), good.toString())
    }

    @Test
    fun `NoAdHocTextStyle flags TextStyle and fontSize but not roles`() {
        val rule = NoAdHocTextStyle(Config.empty)
        val bad = rule.findings(
            """
            fun f() {
                val a = TextStyle(fontSize = size)
                val b = AppText.body.copy(fontSize = size)
            }
            """,
        )
        assertEquals(3, bad.size)
        val good = rule.findings("fun f() { val a = AppText.quranBody }")
        assertTrue(good.isEmpty(), good.toString())
    }

    @Test
    fun `MotionTokensOnly flags literal durations`() {
        val rule = MotionTokensOnly(Config.empty)
        val bad = rule.findings(
            """
            fun f() {
                tween<Float>(300)
                tween<Float>(durationMillis = 250, easing = Motion.standard)
                fadeIn(animationSpec = tween(delayMillis = 100, durationMillis = Motion.fast))
            }
            """,
        )
        assertEquals(3, bad.size)
        val good = rule.findings("fun f() { tween<Float>(Motion.sheet, easing = Motion.standard) }")
        assertTrue(good.isEmpty(), good.toString())
    }

    @Test
    fun `StartEndOnly flags absolute and left right APIs and manual mirroring`() {
        val rule = StartEndOnly(Config.empty)
        val bad = rule.findings(
            """
            fun f() {
                Modifier.absoluteOffset(x = x).absolutePadding(left = p)
                val a = AbsoluteAlignment.TopLeft
                val t = TextAlign.Left
                val r = Arrangement.Absolute.Right
                Modifier.graphicsLayer(scaleX = -1f)
                Modifier.graphicsLayer { scaleX = -1f }
            }
            """,
        )
        assertTrue(bad.size >= 6, bad.joinToString("\n") { it.message })
        val good = rule.findings(
            """
            fun f() {
                Modifier.offset(x = x).padding(start = p)
                val a = Alignment.TopStart
                val t = TextAlign.Start
                Modifier.graphicsLayer { scaleX = 1.12f }
            }
            """,
        )
        assertTrue(good.isEmpty(), good.toString())
    }

    @Test
    fun `NoTextLiterals flags literals templates and concatenation into text`() {
        val rule = NoTextLiterals(Config.empty)
        val bad = rule.findings(
            """
            @Composable fun f(n: Int, name: String) {
                Text("الذكر")
                Text(text = "${'$'}n آية")
                BasicText(name + " آية")
                CaptionText("بيانات مثال")
            }
            """,
        )
        assertEquals(4, bad.size)
        val good = rule.findings(
            """
            @Composable fun f(n: Int) {
                Text(stringResource(Res.string.hub_title))
                Text(text = pluralStringResource(Res.plurals.ayahs, n, formatter.count(n)))
                PrimaryButton(onClick = {}, label = label)
            }
            """,
        )
        assertTrue(good.isEmpty(), good.toString())
    }

    @Test
    fun `InsetsOnlyInOwners allows the owner files only`() {
        val code = """
            fun f() {
                Modifier.statusBarsPadding().imePadding()
                val i = WindowInsets.safeDrawing
            }
        """
        assertEquals(3, InsetsOnlyInOwners(Config.empty).findings(code, "HomeScreen.kt").size)
        assertTrue(InsetsOnlyInOwners(Config.empty).findings(code, "AppScaffold.kt").isEmpty())
        assertTrue(InsetsOnlyInOwners(Config.empty).findings(code, "/repo/core/ui/src/AppScaffold.kt").isEmpty())
        val custom = InsetsOnlyInOwners(TestConfig("allowedFiles" to listOf("Custom.kt")))
        assertTrue(custom.findings(code, "Custom.kt").isEmpty())
    }

    @Test
    fun `NoQuranTextInCode flags vowelled Arabic and Quranic marks but not plain UI words`() {
        val rule = NoQuranTextInCode(Config.empty)
        val bad = rule.findings(
            """
            fun f() {
                val verse = "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ"
                val mark = "قُلْ ۚ"
            }
            """,
        )
        assertEquals(2, bad.size)
        val good = rule.findings(
            """
            fun f() {
                val key = "13:28"
                val word = "الذكر"
                val escaped = "\u064B"
            }
            """,
        )
        assertTrue(good.isEmpty(), good.toString())
    }

    @Test
    fun `the provider registers every rule under the almuflihun rule set`() {
        val ruleSet = AlmuflihunRuleSetProvider().instance(Config.empty)
        assertEquals("almuflihun", ruleSet.id)
        assertEquals(
            listOf(
                "TokensOnly",
                "NoAdHocTextStyle",
                "MotionTokensOnly",
                "StartEndOnly",
                "NoTextLiterals",
                "InsetsOnlyInOwners",
                "NoQuranTextInCode",
            ),
            ruleSet.rules.map { it.ruleId },
        )
    }
}
