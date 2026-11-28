package app.qurancommunity.tooling.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.config
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPrefixExpression
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.KtValueArgument

/** Base for the مجتمع القرآن rules: one issue per rule, reported with the section of the design it enforces. */
abstract class QuranCommunityRule(config: Config, private val description: String) : Rule(config) {
    override val issue: Issue = Issue(javaClass.simpleName, Severity.Style, description, Debt.FIVE_MINS)

    protected fun report(element: KtElement, message: String) {
        report(CodeSmell(issue, Entity.from(element), message))
    }
}

/**
 * DESIGN §0.2, §4, §5: every size, colour and radius comes from `design/tokens.json`. Flags `Color(…)` construction
 * and `.dp` / `.sp` / `.em` on a number literal (`16.dp`). Zero is allowed: it is the absence of a value.
 */
class TokensOnly(config: Config) : QuranCommunityRule(
    config,
    "Raw colours, dp and sp literals are not allowed; use the design tokens (DESIGN §0).",
) {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.calleeExpression?.text == "Color") {
            report(expression, "Raw Color(…): use a colour role from the theme (DESIGN §4).")
        }
    }

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        val unit = (expression.selectorExpression as? KtNameReferenceExpression)?.getReferencedName() ?: return
        val receiver = expression.receiverExpression
        val isZero = receiver.text.trimEnd('f', 'F').toDoubleOrNull() == 0.0
        if (unit in UNITS && receiver.isNumberLiteral() && !isZero) {
            report(
                expression,
                "Literal ${expression.text}: use Space, Radius, Size, Border or a text role (DESIGN §0, §2, §5).",
            )
        }
    }

    private companion object {
        val UNITS = setOf("dp", "sp", "em")
    }
}

/**
 * DESIGN §0.3, §3: text is set only with a role (`AppText.body`, `AppText.quranBody`, …). Flags `TextStyle(…)` and a
 * `fontSize =` argument.
 */
class NoAdHocTextStyle(config: Config) : QuranCommunityRule(
    config,
    "Text styles come only from the text roles (DESIGN §3).",
) {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.calleeExpression?.text == "TextStyle") {
            report(expression, "Ad-hoc TextStyle(…): use a text role from DESIGN §3.")
        }
    }

    override fun visitArgument(argument: KtValueArgument) {
        super.visitArgument(argument)
        if (argument.getArgumentName()?.asName?.asString() == "fontSize") {
            report(argument, "fontSize = …: the size belongs to the text role (DESIGN §3).")
        }
    }
}

/**
 * Durations and easings come from the motion tokens (docs/design-requests.md §3). Flags a number literal passed as
 * `durationMillis` or `delayMillis`, or as the first argument of `tween(…)`.
 */
class MotionTokensOnly(config: Config) : QuranCommunityRule(
    config,
    "Literal animation durations are not allowed; use the motion tokens.",
) {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        expression.valueArguments.forEachIndexed { index, argument ->
            val name = argument.getArgumentName()?.asName?.asString()
            val isTween = expression.calleeExpression?.text == "tween"
            val isDuration = name in MILLIS_PARAMETERS || (name == null && index == 0 && isTween)
            if (isDuration && argument.getArgumentExpression().isNumberLiteral()) {
                report(argument, "Literal ${argument.text} ms: use a motion token.")
            }
        }
    }

    private companion object {
        val MILLIS_PARAMETERS = setOf("durationMillis", "delayMillis")
    }
}

/**
 * DESIGN §2: the app is right to left; start is right. Flags the absolute (left/right) APIs and manual mirroring with
 * `scaleX = -1f`; directional icons mirror in the design system's icon component.
 */
class StartEndOnly(config: Config) : QuranCommunityRule(
    config,
    "Use start/end, never left/right or manual mirroring (DESIGN §2).",
) {

    override fun visitReferenceExpression(expression: KtReferenceExpression) {
        super.visitReferenceExpression(expression)
        val name = (expression as? KtNameReferenceExpression)?.getReferencedName() ?: return
        if (name in ABSOLUTE_APIS) {
            report(expression, "$name is left/right-absolute: use the start/end equivalent (DESIGN §2).")
        }
        val parent = expression.parent
        val onDirectionalType = parent is KtDotQualifiedExpression &&
            parent.receiverExpression.text in DIRECTIONAL_RECEIVERS
        if (name in LEFT_RIGHT && onDirectionalType) {
            report(expression, "${parent.text}: use Start/End so the layout follows right-to-left (DESIGN §2).")
        }
    }

    override fun visitArgument(argument: KtValueArgument) {
        super.visitArgument(argument)
        val isScaleX = argument.getArgumentName()?.asName?.asString() == "scaleX"
        if (isScaleX && argument.getArgumentExpression().isNegativeOne()) {
            report(
                argument,
                "scaleX = -1f mirrors by hand: mirror directional icons in the icon component (DESIGN §7).",
            )
        }
    }

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        val assignsScaleX = expression.operationReference.text == "=" && expression.left?.text == "scaleX"
        if (assignsScaleX && expression.right.isNegativeOne()) {
            report(
                expression,
                "scaleX = -1f mirrors by hand: mirror directional icons in the icon component (DESIGN §7).",
            )
        }
    }

    private companion object {
        val ABSOLUTE_APIS = setOf(
            "AbsoluteAlignment",
            "absoluteOffset",
            "absolutePadding",
            "AbsoluteRoundedCornerShape",
            "AbsoluteCutCornerShape",
        )
        val LEFT_RIGHT = setOf("Left", "Right", "Absolute")
        val DIRECTIONAL_RECEIVERS = setOf("TextAlign", "Arrangement", "Alignment")
    }
}

/**
 * DESIGN §8 and `design/COPY.md`: user-facing text comes from string resources, never literals, templates or
 * concatenation. Flags such a value passed as the text of `Text`, `BasicText` or any `*Text` composable.
 */
class NoTextLiterals(config: Config) : QuranCommunityRule(config, "UI text comes from string resources (DESIGN §8).") {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression?.text.orEmpty()
        if (callee !in TEXT_COMPOSABLES && !(callee.endsWith("Text") && callee.first().isUpperCase())) return
        val arguments = expression.valueArguments
        val textArgument = arguments.firstOrNull { it.getArgumentName()?.asName?.asString() == "text" }
            ?: arguments.firstOrNull()?.takeIf { it.getArgumentName() == null }
        val value = textArgument?.getArgumentExpression()
        if (textArgument != null && (value is KtStringTemplateExpression || value?.isStringConcatenation() == true)) {
            report(textArgument, "Text from a literal or concatenation: use stringResource or pluralStringResource.")
        }
    }

    private fun KtExpression.isStringConcatenation(): Boolean {
        if (this !is KtBinaryExpression || operationReference.text != "+") return false
        return left is KtStringTemplateExpression ||
            right is KtStringTemplateExpression ||
            left?.isStringConcatenation() == true
    }

    private companion object {
        val TEXT_COMPOSABLES = setOf("Text", "BasicText")
    }
}

/**
 * DESIGN §2: system insets (real safe-area plus the design's offsets) are consumed only by the components that own
 * them. Flags inset modifiers and `WindowInsets` anywhere else; the owner files are configured as `allowedFiles`.
 */
class InsetsOnlyInOwners(config: Config) : QuranCommunityRule(
    config,
    "Only the inset-owning components consume system insets (DESIGN §2).",
) {

    private val allowedFiles: List<String> by config(DEFAULT_OWNERS)

    override fun visitKtFile(file: KtFile) {
        // detekt hands rules the path of the file in a real run and the bare name in tests.
        if (file.name.substringAfterLast('/').substringAfterLast('\\') in allowedFiles) return
        super.visitKtFile(file)
    }

    override fun visitReferenceExpression(expression: KtReferenceExpression) {
        super.visitReferenceExpression(expression)
        val name = (expression as? KtNameReferenceExpression)?.getReferencedName() ?: return
        if (name in INSET_APIS) {
            report(expression, "$name outside an inset owner: let AppScaffold, the sheets and the player bar do it.")
        }
    }

    private companion object {
        val DEFAULT_OWNERS = listOf("AppScaffold.kt", "AppSheet.kt", "AyahSheet.kt", "PlayerBar.kt")
        val INSET_APIS = setOf(
            "WindowInsets", "windowInsetsPadding", "statusBarsPadding", "navigationBarsPadding", "systemBarsPadding",
            "imePadding", "safeDrawingPadding", "safeContentPadding", "safeGesturesPadding", "displayCutoutPadding",
            "consumeWindowInsets", "windowInsetsTopHeight", "windowInsetsBottomHeight",
        )
    }
}

/**
 * DESIGN §1 and the README's rules: Quran text is shown only from the verse data, never typed into code. Flags a
 * string literal containing Arabic diacritics (U+064B–U+065F, U+0670) or Quranic annotation marks (U+06D6–U+06ED),
 * which UI copy never needs. Plain Arabic UI words are not flagged; they belong in string resources anyway.
 */
class NoQuranTextInCode(config: Config) : QuranCommunityRule(
    config,
    "Quran text comes only from the verse data, never from code (DESIGN §1).",
) {

    override fun visitStringTemplateExpression(expression: KtStringTemplateExpression) {
        super.visitStringTemplateExpression(expression)
        val literal = expression.entries.filterIsInstance<KtLiteralStringTemplateEntry>().joinToString("") { it.text }
        if (literal.any { it.isQuranicMark() }) {
            report(
                expression,
                "Vowelled Arabic or Quranic marks in code: load the verse from the verse data (DESIGN §1).",
            )
        }
    }

    private fun Char.isQuranicMark(): Boolean =
        this in '\u064B'..'\u065F' || this == '\u0670' || this in '\u06D6'..'\u06ED'
}

private fun KtExpression?.isNumberLiteral(): Boolean = when (this) {
    is KtConstantExpression -> text.firstOrNull()?.isDigit() == true
    is KtPrefixExpression -> operationReference.text == "-" && baseExpression.isNumberLiteral()
    else -> false
}

private fun KtExpression?.isNegativeOne(): Boolean =
    this is KtPrefixExpression && operationReference.text == "-" &&
        baseExpression?.text?.trimEnd('f', 'F')?.toDoubleOrNull() == 1.0
