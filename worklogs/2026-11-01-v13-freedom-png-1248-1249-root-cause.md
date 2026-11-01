# V13 1248/1249 freedom PNG blockage — verified
2026-11-01 Asia/Seoul

Captain reported: phone Freedom room shows not a proper results PNG, sees only three groups/sets, cannot analyze round 1248 or 1249.

## Actual source audit
- Public live remote UI revision: 19; latest live APK version: V13 R8.29.
- V13 APK assets/db1_master_1_1248.json has records 1..1248 and valid six-text RAW for round 1248.
- V13 APK DB1 does not have round 1249 by default; optional local device overlay is unknown and must not be assumed missing on every device.
- Preserved R19 study engine source nav_v33_r19_three_origins_two_auto.js has explicit `n>1247` guard.
- Freedom PNG renderer is gated behind `acceptedRoomPredraw('freedom',round)` (actual 15 ticket receipt), or historically limited R19 research preview up to 1247.
- 3 groups RAW5/AUTO_A5/AUTO_B5 are 3 *groups* of 5 = 15 planned output slots, not 3 verified tickets.
- Stored 2026-10-30 V12 pre-registration JSON for 1248 and 1249 explicitly says PRE_DRAW_SOURCE_NOT_VERIFIED, db1_raw_six_lines=null and no engine run. Do not relabel as formal pre-draw results.
- Not all 4 certified V12 original engine adapters are connected, so real 30 tickets and official outcome grading are not end-to-end available.

## Code candidate (NOT LIVE)
- releases/staging/v13-ui-r20-freedom-png-reason.html
- Git source blob 45365e26cbc15262bfb7dd6e4c0141af0ef60501
- Corrects stale document revision label to 20, explains exact 1248/1249 RAW/engine gate in freedom room, labels three planned five-ticket groups clearly.
- JavaScript 2 inline scripts parsed successfully. Original DB1, prior predictions, official scoring guard, APK, manifest, engines preserved.
- No fake PNG created. Full 15 authentic Freedom tickets cannot be generated without source/verified engine connection. Physical test pending.
- Main release manifest remains revision 19; explicit approval required before claiming live update.

## 2026-11-01 captain correction — latest preserved DB-1 source exists for 1249
- Earlier “DB-1 has no 1249 RAW” conclusion applies ONLY to the bundled V13 APK baseline (`assets/db1_master_1_1248.json`), NOT to all latest DB-1 source records.
- Read preserved Drive `DB1_1248_1249_V12_V3_소스패치_검증팩_20261030.zip` (Drive file id `1hcuYuUDPYFVY5UduDjcKl6a9zFplKY-a`; archive SHA256 `6c6480f90bc67692a8821d224933100a37176c912c4a2af685f6b20136467ebd`). `raw_1248_1249.json` includes six RAW sentences for each 1248 and 1249, and `db1_v1249_append.js` supplies an add-only path through 1249.
- Reran archive-provided Node tests in isolated local temp directory: `node test_patch.cjs db1_v1249_append.js raw_1248_1249.json`: PASS first insert / idempotent repeated insert / conflict HOLD. `node test_real_assets.cjs`: PASS standalone and embedded, count 1249. These tests do NOT verify a live V13 Android deployment or phone DB overlay.
- The older `1248_1249_실전회차_사전등록.json` saying `db1_raw_six_lines:null` is a PRE-DRAW HOLD record dated 2026-10-30, not evidence that 1249 RAW is absent from the preserved source patch.
- Corrected next diagnosis: do not ask captain to re-enter 1249 RAW. Check whether V13 current DB reader sees the preserved/latest record and whether the formal engine adapters support 1248+; R19 guard `n>1247` remains an independent engine blocker. Do not label research fallback as official 15/30-ticket PNG.
- No live manifest, APK, production DB, locked predraw reports, approval records, or published remote revision changed by this documentation correction.
