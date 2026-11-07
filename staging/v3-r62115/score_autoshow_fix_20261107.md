# V3 score auto-display repair — 2026-11-07

Candidate patch is based on the available V3.0.83 R3 `index.html`; it has **not** been verified as the exact installed R6.21.14 source.

## Repaired interfaces
- On `analysisResultPage()`, display a read-only score summary if the current round has six valid saved winning numbers; otherwise show '미채점' instead of any fabricated result.
- `v3OpenScoreForAnalysis()` opens the current analysis round's detailed 4-room score view.
- `scorePage(round)` defaults to the current working round when invoked with no argument, avoiding stale selection from prior browsing.
- All number-generation functions, `storeAnalysisResult()`, local storage keys/schemas, old DB, and 1249 purchase combinations remain unchanged.

## Verification
- Node syntax checked complete extracted inline JS: PASS
- Node sandbox test for pending/graded/round navigation: PASS
- Static exact comparison of generator and analysis store: PASS

## Deployment status
No production file edited, no APK built, no signing certificate matched, no device installation tested. Preserve R6.21.14 as purchased round baseline. Do not merge to main prior to source/signature verification.

Local archived candidate: `V3_SCORE_AUTOSHOW_FIX_20261107.zip`.
