# ⚓ V13 R16 — Captain engine asset staging + APK signing identity QA (2026-11-01)

## New verified findings
The previously located V12 Captain engine was **actually run** in Chromium without modifying its code. DB-1 six source sentences match V13 APK exactly for 1..1247, all 1247 rounds. Eight runs over 1241, 1242, 1244, 1247 returned four deterministic five-game historical results. See `worklogs/2026-11-01-v13-r16-v12-captain-real-engine-e2e.md`. This proves only source execution for historical rounds, not predictive performance or official pre-draw lock.

Both preserved signed V13 APKs were inspected from actual APK v2 signing block and compiled `AndroidManifest.xml`:
| APK | SHA256 | package | versionCode | signer certificate SHA256 |
|---|---|---|---|---|
| R8.14 captain baseline | `13e464d3d77631026a8474b9d49ea0d2b3aabf6bbca06aef358ede25dd649169` | `com.baekhak.dosa.v13r3preview` | `31322` | `3fe57967c15f81ee39ea2b9b88dd96a7506c1b5b9032606ecbea04bbdb15ee3c` |
| R8.29 official current APK | `6ccd9440ff79efb12ab8415de7922179f9a0ff71e8e64befa1ec3d7e3f1c4f11` | `com.baekhak.dosa.v13r3preview` | `31337` | `3fe57967c15f81ee39ea2b9b88dd96a7506c1b5b9032606ecbea04bbdb15ee3c` |

The package, monotonic versionCode, and certificate identity match the key *structural* prerequisites for an Android in-place update from R8.14 to R8.29. This is **not** a cryptographic APK verifier substitute (`apksigner` unavailable), and device installation/data preservation remain **unverified**. The signer is an Android Debug certificate; to publish a future compatible APK, the **same private signing key** is required, merely recreating a debug key with the same DN will NOT work.

## Non-destructive local asset staging
Created `V13_R16_V12_선장엔진_원본자산_비배포준비본_20261101.zip` (5,877,045 bytes, ZIP CRC pass) containing only verified originals from V12:
- `app/src/main/assets/v10_engine/engine_v10.html`: sha256 `b2ff0097896f246f5ca885afb07dcf8841b494ceef1a91497901681504c208bd`;
- `app/src/main/assets/v10_engine/ENGINE_V10_LOCK.json`: unmodified original limits/attestation;
- `app/src/main/assets/engines/v10_isolated_captain_bridge.js`: sha256 `b2fbcf6fac2716eb411084ef825b409b6205eca51eade91281ff270ab9d68240`;
- `ASSET_STAGING_VERIFICATION.json` and integration checklist.

Original engine bytes are not copied into this public repository: they remain under owner's archived V12 ZIP and local user deliverable. No existing V13 source, ZIP, APK, signing key, DB, or user localStorage was overwritten.

## Next bound work
1. Locate confirmed latest V13 complete AndroidIDE source and compare with R8.29 APK assets; apply staging assets to a **new copy only**, never to production originals.
2. To connect V13, retain the genuine bridge message contract and use the existing branch `research/v13-r16-engine-contract/v12-captain-research-adapter.cjs` in an eligible historical research test (<=1247 only). Keep research results excluded from official 30.
3. Locate signer owner credentials, verify with `apksigner` where available, compile staged APK and install on a real device preserving data. No new APK produced in this stage.
4. Acquire an independently validated current-round captain source and pre-draw attestation. Current V10 is historical-only. Do not relax engine range restrictions.
5. V3 owner MASTER, V7 E100, V12 originals, DB-1, owner approval history, remote 15 all preserved.

## Release protection readback
GitHub `main/releases/v13-release.json` remains V13 R8.29 (31337) and remote UI revision 15; no R16 release has been published.
