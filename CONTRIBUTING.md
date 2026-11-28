# Contributing to مجتمع القرآن

Thank you for wanting to help. This is a non-profit app used by real people to read the Quran, so correctness and care come before speed.

## Before you start

1. Read [CLAUDE.md](CLAUDE.md): the rules for every contributor, human or AI.
2. Read the [plan](docs/plan.md) and the [ADRs](docs/adr/README.md). Accepted ADRs are binding.
3. For anything visual, the [design](design/README.md) is binding. If it does not cover your case, open a **design request** issue instead of inventing a solution.
4. Follow the [Code of Conduct](CODE_OF_CONDUCT.md).

## Ways to help

- **Report a Quran content error** (wrong verse, cut verse, wrong reference): use the *Quran content error* issue form. These are fixed first.
- **Report a bug** or **suggest a feature** with the issue forms.
- **Pick an issue** labelled `good first issue` or `help wanted` and say you are working on it.

## Making a change

1. Fork, then branch from `main`: `feat/<module>-<short-name>` or `fix/<module>-<short-name>`.
2. Keep the change small and focused; one screen or one concern per PR.
3. Commit with [Conventional Commits](https://www.conventionalcommits.org/): `feat(mushaf): add saved-page ribbon`. The scope is the module name.
4. Run the checks locally (commands in `CLAUDE.md`), including screenshot tests.
5. Open a PR. Fill in the template, including the [quality bar](docs/guides/quality-bar.md) checklist.
6. An automated review comments first; a maintainer reviews and merges. `main` accepts squash-merged PRs only.

## Proposing a decision

New library, pattern, or a change to an accepted decision: copy `docs/adr/0000-template.md`, fill every section with at least two real options, and open a PR titled `docs(adr): propose NNNN <title>`.

## Licence

By contributing you agree that your code is released under the [MIT licence](LICENSE). Third-party content keeps its own terms ([THIRD_PARTY.md](THIRD_PARTY.md)).
