package app.almuflihun.tooling.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

/**
 * The design's rules (`design/DESIGN.md`) and the Quran rules (`CLAUDE.md`) as lint (ADR 0001, ADR 0011). Each rule
 * names the section it enforces; where it applies is set in `config/detekt/detekt.yml`.
 */
class AlmuflihunRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = RULE_SET_ID

    override fun instance(config: Config): RuleSet = RuleSet(
        ruleSetId,
        listOf(
            TokensOnly(config),
            NoAdHocTextStyle(config),
            MotionTokensOnly(config),
            StartEndOnly(config),
            NoTextLiterals(config),
            InsetsOnlyInOwners(config),
            NoQuranTextInCode(config),
        ),
    )

    companion object {
        const val RULE_SET_ID = "almuflihun"
    }
}
