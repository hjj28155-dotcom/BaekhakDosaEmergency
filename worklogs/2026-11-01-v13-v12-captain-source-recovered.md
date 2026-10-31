# 2026-11-01 V13 R16 — V12 captain original located / research gate

The Captain correctly pointed out that V12 carries the captain engine. Direct evidence:
- Drive V12 R24 source ZIP `V12_N13_R24_31224_소스검증인계팩_20261030.zip` has `assets_index.html`, which explicitly includes `engines/v10_isolated_captain_bridge.js` and `v10_engine/engine_v10.html`, plus `NAV_V3_R34_CAPTAIN.request(round,raw)`.
- Drive V12 R9 full Android project ZIP `V12_R9_네이티브기능연결_컴파일계약검증_20261030.zip` is **18,204,404 bytes**, SHA256 `5d61894327e26434a8c7aaf092ac86117d9047fbf5a6061054264b8a05adf5f6` and archive CRC PASS.
- It actually includes `app/src/main/assets/v10_engine/engine_v10.html`, 8,734,111 bytes, SHA256 `b2ff0097896f246f5ca885afb07dcf8841b494ceef1a91497901681504c208bd`.
- It actually includes `app/src/main/assets/engines/v10_isolated_captain_bridge.js`, 7,028 bytes, SHA256 `b2fbcf6fac2716eb411084ef825b409b6205eca51eade91281ff270ab9d68240`.
- `ENGINE_V10_LOCK.json` confirms V10 isolated R60 fork from R59 original U10 and specifically says historical predictions are NOT independently certified. This is source provenance, not official prediction proof.
- V12 original bridge does `V10_ANALYZE -> V10_ANALYSIS_RESULT`, validates gate PASS / 5 combinations, and rejects round >1247. Its `NAV_V3_R34_CAPTAIN` alias should not be mistaken for the separate installed V3 MASTER app.
- V13 R8.29 (31337) APK DOES NOT contain V10 isolated captain files, hence its remote UI cannot invoke this physical original without an APK asset update.
- Browser end-to-end run was attempted with extracted V12 original and a local harness but sandbox administration blocked navigation (`ERR_BLOCKED_BY_ADMINISTRATOR`). No real engine execution success, signed APK update, future-round 5 combinations, or device PNG is asserted.

Candidate: `research/v13-r16-engine-contract/v12-captain-research-adapter.cjs`. Guarded read-only adapter for 1..1247: requires explicit external source-hash verification, bridge identity, gate PASS, five unique six-number sets, and excludes derived fallback. Returns researchOnly=true; official false; does not change locks, DB or production UI. Dummy-response tests only, not proof of live V12 output.

NEXT:
1. Run source engine on permitted Android WebView/browser runtime using actual genuine six-sentence RAW for eligible historical rounds. Verify message and stable five outputs; record hashes.
2. For round 1248+, select a separately validated supported engine revision: DO NOT bypass original <=1247 bridge limit or backdate historical draws.
3. Compare version, signatures and packaged assets before building new APK. Update V13 manifest only after APK exists and device tests pass.
4. Keep V3 MASTER, V7 and V12 originals separate; do not alter APK, DB-1, approval or signature keys.

THIS TURN: research artifacts only, never deployed to main.
