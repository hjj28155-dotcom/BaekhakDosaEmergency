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
