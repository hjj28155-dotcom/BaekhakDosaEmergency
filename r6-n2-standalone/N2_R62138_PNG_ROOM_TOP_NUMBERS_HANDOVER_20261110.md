# ⚓ N2 R6.21.38 PNG top-of-room predictions handover (owner pilot; not live)

Captain requires predicted numbers at TOP of each of 4 rooms in the PNG to distinguish candidate coverage 6/6 from actual 6/6 combination.

## Accurate provenance limitation
The existing saved `record.rooms[].games` stores only five final games per room. **Independent pre-combination source candidate lists have NOT been durably persisted per room**. Therefore 6.21.38 displays the **union of actual numbers used in the five stored games**, explicitly labeled `5조합에 사용된 번호 전체`, and NOT the original engine candidates. It calculates union-vs-winning-number match n/6; separate game hit counts remain below. Never reconstruct original candidate lists using post-draw results.

## Build and tests
- Base 6.21.37 pilot (includes 1250/1251 analysis limit fix).
- PNG expanded to 1280×1160; top-of-room union strip, match count, red hit balls and blue bonus; existing 20 games below. Same in app score-room cards.
- JavaScript syntax 32/32 PASS; actual Chromium PNG render and top-of-room DOM smoke PASS.
- Signed 6.21.38 (621938) APK: SHA256 `c5e72139e9cecc0ba06ce4f1be9b3e4a37b0749ccb88c9dbe2d8de46e674650c`, 2,281,785 bytes.
- Official Android apksigner --min-sdk-version 24 PASS, v2 signature true; cert SHA256 `81d75da8b4e1c82fbe8af27a0f8512236f2b1c2cdef5d7d015f695fb98893d04`.
- Changed APK ZIP entries only assets/index.html, AndroidManifest.xml. No local app data read/deleted/initialized.
- Owner-only APK on Drive: https://drive.google.com/file/d/1r5F2r1hyW_gWlDubDzs4kh9xhT47bUSz/view
- Full Korean record: https://drive.google.com/file/d/1B1bO4QW-iGyzHROTzRo8rjH1wuji_z0T/view

## Current state and next steps
- **Main N2 update.json is still live at version 6.21.35. No 6.21.38 deployment.**
- Device PNG download and record retention not yet tested.
- Next engine upgrade must persist independent pre-combination `source_candidates` in an additive future-record field, tagged with provenance and generated-before-draw timestamps. For existing records explicitly show original-candidate NOT RECORDED. A/B compare true pre-combination coverage 6/6 and game-level 6/6 across same 100 rounds, without hindsight leakage.
- Keep legacy R6, Lotto645, V3, V7, all historical backups, and private signing keys separate.
