# V13 / V12 Captain R34 bridge runtime audit — 2026-11-01

Source: Google Drive V13 R8.15 full AndroidIDE ZIP (75,818,575 bytes).

Verified V12 preserved v3_original_r62/index.html already loads engines/nav_v3_r34_captain_bridge.js AND hidden iframe v3r34CaptainEngine with src engine_v2.html. The previous hypothesis of a missing R34 script in V13 main is not sufficient as an explanation: the preserved V12 view itself loads it.

Actual R34 version: V3_R59_CAPTAIN_AUDITED_RESEARCH_BRIDGE_20261028. It accepts rounds 1..1247 only, raw six nonempty single-line strings, and requires live iframe response with packGate PASS, packLocked true, 7-10 unique ranked valid numbers and five valid ticket combinations.

Executed Node VM harness using actual bridge code: round 1248 rejected, round 1249 rejected, packGate HOLD rejected, empty ticket set rejected: 4/4 tests PASS. No lottery combinations were fabricated or stored.

Therefore 1248/1249 lack authentic captain production output via this R34 path. Removing gate/round limitations without proof of supported engine version would be unsafe. Need the V12 certified production engines for captain/dosa/freedom and the separately uploaded navigator production engine.

Unmodified: installed APK, GitHub release manifest revision15, local DB, predraw records, approvals and locked sources. No APK build/device test. The 4-way 30-combo pipeline remains unverified.