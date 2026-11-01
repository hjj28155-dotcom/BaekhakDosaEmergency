# ⚓ V13 R8.29 / remote UI revision 17 — 2026-11-01 KST

## Captain's explicit requirements
- Home > 원문 분석 실행 launches 4 independent rooms together: Captain 5, Dosa 5, Freedom 15 (RAW5/AUTO_A5/AUTO_B5), Navigator 5.
- Each room should show its actual original-result combinations.
- 성적관리 needs 30 rows, automatic after-draw winning numbers and bonus comparison, 1-5 grades/낙선, winner congratulatory text and fanfare, explicit all-loss message.

## Public opt-in UI version
- APK unchanged: V13 R8.29 versionCode 31337, package `com.baekhak.dosa.v13r3preview`.
- HTML: `releases/ui/v13-ui-r17.html`, Git blob `839cc1a46cf324aa798c416c8311ab97fc880c19`.
- UTF-8 byte count 1,618,414. SHA256 `ecc8c71c95cab2c887149a219d21a6cfbfeb9c72bfed353602d765ac6a913856`.
- Release metadata `releases/v13-release.json`: uiUpdate.revision 17, deploymentMode `remote_ui_opt_in`, minVersionCode 31337.
- Existing APK download URL, DB-1, original V12/V3/V7 code, saved result-before entries, cumulative ledger and signatures unchanged.
- Retained `releases/ui/v13-ui-r16.html` unmodified for rollback.

## Implemented and tested
1. `runConnectedPredraw` now schedules explicit 4 designated room calls concurrently and reports per-room actual source status (successful source, missing engine, research-only, error).
2. 30 slot rows visible in score menu (5+5+15+5); unreceived slots say `미수신`, not invented numbers.
3. Changed 2026 official per-round query to `/lt645/selectPstLt645InfoNew.do?srchDir=center&srchLtEpsd=` and still demands exact matching round and official source.
4. Official number and accepted formal 30 required for grade. 1/2/3/4/5/0 test fixtures passed, public-mirror-only blocked, round mismatch rejected.
5. In the SCORE MENU only, winning 30 group grade displays congratulatory banner and dedicated manually tapped fanfare button; all-loss displays explicit `낙선` and non-winning message. Existing best-effort one-time fanfare after newly verified result retained; Android auto-audio permission is device dependent.
6. Test harness checked 4 independent Promise.allSettled results: captain mock report succeeds, Dosa engine exception isolated, Freedom missing engine flagged, Navigator research rejected. No fabricated formal tickets accepted.
7. JS script parse 2; 11 preserved static guards. GitHub post-publish blob and manifest re-read: 10/10 successful.

## Unresolved — do not claim full completion
- V12 R9 source inventory shows `dosa_v2_4_rc_original.js` gated to historical 1..100 until pass, R19 research only, R34 captain frame bridge research needing a verified original reply; V12 R9 manifests explicitly say not prospectively validated.
- Actual production V12 captain/dosa/freedom 5+5+15 still lack authenticated V13 result-before adapters. Therefore actual 4-engine 30 real tickets / 1249 round output and WIN/LOSS in user's device are NOT yet proven.
- A real Android WebView may prevent cross-origin official JSON retrieval and auto-audio; on-device fetch / tap and score render must be verified.
- No new APK, signing, installed-device screenshot or certified time-stamped before-draw lock test in this work.

## Actual phone test
Open V13 R8.29 > accept optional remote UI rev17 > 원문 분석 > select an existing DB-1 RAW6 round > 원문 분석 실행 > verify 4-room status > 성적관리 shows 30 numbered slots, correct missing status, official result fetch status. 30-combo full scoring/celebration only when authentic four room results and official number are successfully verified.

## Rollback
Retain rev16 HTML blob `6267fe826baa840241d32e5cae13f724bb510613`. Previous manifest was revision16; if rev17 has a regression, rollback the manifest reference only after checking client caching, do not delete source/history or local DB.
