# ⚓ N2 R6.21.37 — 1250/1251 analysis and per-room predicted-number display handover

**Date: 2026-11-10 (KST)**

## Captain's observed defects
1. N2 **6.21.35** owner-device V1 update succeeded, but **round 1250 did not analyze**.
2. Captain noticed **expected numbers for each room appeared missing**.

## Verified causes
- Two analysis engines retained a `round > 1249` guard despite 1250/1251 raw-source bank entries.
- The four-room view supported five number combinations per room, but upon successful analysis automatically navigated to the score page, which **did not show all stored predictions from the four rooms**.

## Fix staged (NOT released)
- 6.21.36: extend two engine round-limit checks and last-round restore bound to 1251.
- **6.21.37**: retain 6.21.36 fix, add explicit prediction cards to score page:
  - `captain` / `dosa` / `cross` / `diverse` — 5 saved six-number sets each, 20 sets total.
  - Numbers rendered solely from valid saved `record.rooms[].games`, no new numbers invented or record mutations.
  - If no saved record, clearly show a no-analysis notice.

## Test evidence
- JavaScript syntax 32/32 PASS.
- Chromium isolated room display check: 4 room cards, 20 sets, 120 number elements; empty-record message PASS.
- Prior 6.21.36 engine direct tests: 1249, 1250 and 1251 round PASS / four rooms × five games.
- APK ZIP CRC, signature v2 cryptographic digest PASS; official Android apksigner verify min SDK 24 PASS.
- Only `assets/index.html` and `AndroidManifest.xml` differ from signed 6.21.36 APK.
- APK: `com.hanghae.fold8n2app`, version `6.21.37` / `621937`; size 2,281,159 bytes.
- SHA-256 `71f9a9b54234de9043cd80fea258e945efa6260006b31797a0559962a129202f`.
- Signing certificate SHA-256 `81d75da8b4e1c82fbe8af27a0f8512236f2b1c2cdef5d7d015f695fb98893d04` (permanent N2 cert).

## Stored owner-only Drive files
- Signed pilot APK: https://drive.google.com/file/d/1SGhIRl6APTABPO5_xgebyQAMMRfWqgbx/view
- Complete Korean handover: https://drive.google.com/file/d/1RxsqzprcxDEHviaNVc4G5iKotvNpmVFe/view

## Important limits
- Live V1 update manifest `main:r6-n2-standalone/update.json` still **6.21.35**, not 6.21.37.
- Full analysis UI + durable storage could not be completed in one browser test environment due to IndexedDB access restriction; owner Samsung phone test still needed.
- Existing device data untouched by work. No legacy R6 / Lotto645 / V3 / V7 changes.
- Signing private key and password never stored in GitHub or reports.

## Next
Upon captain's instruction: use 6.21.37 pilot, verify in-place update and 1250/1251 analysis with per-room 5 sets on device; then proceed to unchanged 100-round engine performance baseline. Preserve all existing data and backups.
