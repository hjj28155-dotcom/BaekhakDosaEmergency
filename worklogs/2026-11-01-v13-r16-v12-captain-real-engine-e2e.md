# ⚓ V13 R16 — V12 Captain engine real execution and DB-1 cross-check
Date: 2026-11-01 KST. **Research branch only; no deployment.**

## Actual source / integrity
- Preserved Drive ZIP: `V12_R9_네이티브기능연결_컴파일계약검증_20261030.zip`, sha256 `5d61894327e26434a8c7aaf092ac86117d9047fbf5a6061054264b8a05adf5f6`.
- `app/src/main/assets/v10_engine/engine_v10.html` sha256 `b2ff0097896f246f5ca885afb07dcf8841b494ceef1a91497901681504c208bd`.
- `app/src/main/assets/engines/v10_isolated_captain_bridge.js` sha256 `b2fbcf6fac2716eb411084ef825b409b6205eca51eade91281ff270ab9d68240`.
- V13 R8.29 APK already contains exactly the V12 `raw_bank.js` bytes in both `assets/v12_v3_master/` and `assets/v3_original_r62/`. SHA256 `50ec8adeb6c4fe4d33d28e3ae6ad92acf0af8e167b1d43b57a97b0c1e50e0c6f`.
- Full comparison of **all 1,247 rounds**, six source sentences each, between V12 `V3_RAW_BANK` and V13 `db1_master_1_1248.json`: **1,247/1,247 exact equal**.

## Real offline browser execution (not mocks)
Initial localhost navigation returned `net::ERR_BLOCKED_BY_ADMINISTRATOR`. We resolved sandbox testing restrictions **without changing original engine code** by using Playwright Chromium `page.set_content` to load source into an `about:blank` iframe and by injecting an in-memory `localStorage` shim into the isolated test frame. This is **not** device/WebView proof, nor durable real DB lock.

The unchanged V12 original `v10_isolated_captain_bridge.js` sent `V10_ANALYZE` for actual V13 DB-1 six-sentence texts; the unchanged original `engine_v10.html` responded `V10_ANALYSIS_RESULT`, verified by original bridge gate checks.

| Round | Run 1 | Run 2 | rank count | 5-game SHA256 prefix |
|---|---|---|---:|---|
| 1241 | PASS 5 | PASS 5 | 10 | `ee218237e60319bd` |
| 1242 | PASS 5 | PASS 5 | 10 | `b17c8c2e41e7ccff` |
| 1244 | PASS 5 | PASS 5 | 9 | `6da6b9cd646a0b97` |
| 1247 | PASS 5 | PASS 5 | 8 | `ad3b3e495cf58841` |

Across all eight actual engine invocations: `packGate=PASS`, `packLocked=true`, `localU10Validated=true`, `fallbackResearch=false`; 5 unique valid combinations per round, only from ranked candidates; same game-array SHA256 across both runs per round. Round 1248 was rejected with `CAPTAIN_INPUT_INVALID` (do not bypass).
The source lock file `ENGINE_V10_LOCK.json` expressly does **not** claim certification of historical or official predictions. These are *historical engine regression tests*, not official results.

## Gate / next steps
- Prior GitHub V13 research adapter remains **research-only**, requiring source-hash attestation and original <=1247 gate. This direct test proves the legacy source runs; it does not make the 1248+ production engine available.
- V13 R8.29 installed APK currently **omits** this engine HTML and bridge. An opt-in remote UI cannot supply the missing local APK assets or add a signing key; a new correctly signed Android package is needed for production integration.
- Build a **non-destructive asset integration plan** for a research APK; verify original V13 signature/owner credentials and installed data retention before a release; never overwrite main manifest or user's installed app.
- Require independently verified current-round original engine, BEFORE time lock, source fingerprint, and official 6+bonus before 30-set official scoring.
- V3 `com.baekhak.dosa.v3` / V7 / DB-1 / app approvals and original V12 are preserved.
- Local complete reproducible QA bundle: `V13_R16_V12_선장엔진_실행검증팩_20261101.zip` (script, SHA256-only result and findings). **No private original code or RAW sentences are copied into public GitHub.**
