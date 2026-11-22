# V75326 후보수 사후 복기 2차 (2026-11-23)

- Input is saved actual `deep_candidate_audit_801_1250.json`, 450 walk-forward rounds 801–1250, one mock device seed, not fresh holdout.
- V75320: best-room top12 contains >=3 winners in 221 rounds, final 20 tickets >=3 in 142; gap 79. Top12 >=4 in 51 rounds, final >=4 in 9; gap 42.
- V75325: best-room top12 >=3 in 258, final >=3 in 124; gap 134. Top12 >=4 in 63, final >=4 in 8; gap 55.
- Distinguish potential loss from top12->top10 shrink, per-room 5-ticket selection, and inability to recombine across independent rooms. Aggregates alone do not identify sole root cause.
- Theoretical random candidate12 expected winning number coverage=1.6, P(>=3)=0.1828, P(>=4)=0.0354; engine per-room coverage remains around those values. No proven predictive superiority.
- Detailed audit/CSV report: https://drive.google.com/file/d/1YDczpP8eIGw3qyB9GBQqHyvVhL5h4FJY/view
- NO APK change, no home/UI change, no user data reset, no deployment. Official release V75313 remains. Next: per-round top12->top10->final5 trace, untouched future holdout.
