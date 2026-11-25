# V13 rev17 — 4-room analysis, auto score, fanfare (STAGING ONLY)
- Baseline live rev16 Git blob 6267fe826baa840241d32e5cae13f724bb510613.
- Uses four-room Promise.allSettled with exact room keys; connected engines can return authentic reports while unconnected/legacy research remain marked as not received. No fabricated original tickets.
- Shows 30 slots in score menu with missing numbers labelled, never invented.
- Official source query corrected to `selectPstLt645InfoNew.do?srchDir=center&srchLtEpsd=ROUND` with exact-round response match.
- Existing official source/verifiedOfficial scoring guard unchanged. Win displays congratulations, winning grades and manual fanfare button; loss displays explicit 낙선 message. Audio autoplay may be blocked on some WebViews.
- Never changes original V12/V3/V7 engines, PREDRAW_STORAGE, DB-1, cached saved reports, or published APK.
- Static guards 11/11, script syntax 2 scripts parsed. No real four-engine production output or device test yet.
- NEXT: test draw API/CORS in Android WebView, validate source provenance and actual 5+5+15+5 output before publishing or calling official result-before.

## Runtime mock verification
- New per-round official-result parser: exact round accepted; mismatched 1249 rejected; public-mirror data correctly retained as unverified.
- Lotto ranking examples 1/2/3/4/5/0 grades checked; winner and all-loss 30-combination test frames passed.
- Scoring blocked for mirror-only or incomplete formal 30 tickets.
- Score menu banner: 30 accepted test sets produced congratulations and a manual fanfare button; all-loss produced '낙선' message without that button; pending showed neither.
- Four-room async test: captain valid response recorded, dosa rejected as engine error, freedom marked disconnected, navigator research refused; 4-room Promise.allSettled isolated failures correctly.
- These tests use synthetic reports ONLY and do not demonstrate actual certified four-engine outputs, browser CORS, or real handset playback.
