# ⚓ N2 E1 — 201–300 retrospective A/B finished (2026-11-11)

**Production N2 R6.21.41 remains unchanged; NO APK published, user DB left intact.**

## Input provenance
- 201–300 DB-1 raw 6 sentences per round found embedded in owner N2 R6.21.41 HTML, extracted 100×6 without modifying source.
- 1–200 prior draws from latest N2 backup's 200 verified rounds.
- 201–300 historical winners from third-party CC BY 4.0 https://github.com/Loisirio/lottochaos-lottery-data/blob/main/data/kr_lotto.csv , blob f3249805be41b8f4a7785bb4dba65a462202c79c. NOT independently verified all 100 against official draw pages.
- During each round r, engine history accessor returned only draws < r, never r or later; target draw reserved for post-hoc scoring.
- Compare already-frozen baseline R6.21.41, E1-A2 lexical exception patch, E1-C 5-combination optimal 75-quartet design, and A2+C.

## Verified browser run
- Both HTML sources ran 100 rounds × 4 rooms with no JS error.
- Each room candidate count 10, five 6-number games, all from its own pool. Four-room test count 400.

| Variant | pool 4+ | room best 3+ | room best 4+ | room best 5+ |
|---|---:|---:|---:|---:|
| R6.21.41 | 9 | 35 | 4 | 0 |
| R6.21.41 + E1-C | 9 | 36 | 4 | 0 |
| E1-A2 | 8 | 38 | 3 | 0 |
| E1-A2 + E1-C | 8 | 38 | 4 | 0 |

- Baseline 4+ draws: 206 dosa, 221 captain, 235 dosa, 271 captain.
- Baseline + E1-C 4+ draws: 206 dosa, 221 captain, 224 dosa, 239 diverse.
- Candidate 4+ by room baseline: captain 2, dosa 4, cross 0, diverse 3.
- 10-number random candidate 4+ theoretical expected across 400 tests 6.58; observing 9 has binomial reference upper-tail ~0.217; no proof of predictive edge. Dependence between room evaluations is not captured by binomial reference.
- **Decision: neither E1-A2 nor E1-C has demonstrated a 4+ improvement relative to production; both remain in research and not deployed.** Historical retrospective study, NOT evidence of genuinely pre-draw prediction.
- Next 301–400 separately, plus random benchmark & evidence quality audit; don't tune on 201–300.

Local report: N2_E1_201-300회차_실측검증_인계_20261111.md
Local details: N2_E1_201-300회차_원문기반_동일조건_AB_재실행결과.json
Local raw: N2_E1_201-300회차_DB1_원문100회_읽기전용추출.json
Local referenced draws: N2_E1_201_300_PUBLIC_DRAW_REFERENCE.csv
