# V13 R16 — V12 original three-component transfer candidate

2026-11-01 / **research branch only**.

The captain requested preservation and connection of three V12 components in V13: the standalone Freedom V4 app, original DOSA engine, and original V10 Captain engine.

The Freedom V4 app remains a **separate installed Android package**, not a rewritten iframe. The V13 candidate includes an explicit intent launcher to the existing V4 component. Preserved V4.0.54 APK is marked TEST_ONLY and was not deployed.

The original V10 engine, V10 isolated bridge and lock metadata were copied byte-for-byte. The original 1..1247 bridge range remains unchanged. DOSA V2.4-RC was already bundled in the R8.29 APK with identical SHA-256 and remains a historical 1..100 research engine.

A separate R8.29 embedded-UI research entrypoint and a V4 native-Java patch candidate were prepared. Node inline JavaScript syntax passed; Java compiled using stubbed Android APIs; isolated Chromium exercised the true V10 engine (1241, five tickets and PNG), true DOSA engine (round 1, five tickets; 101 blocked), and the V4 launch button with a mock native callback. Browser errors: zero; official before-draw storage writes: zero.

**No new APK, device install, live V4 launch, latest-round engine certification, official result scoring, or production UI deployment.** The current R8.29 full Java source is not verified, so the reconstructed older Java candidate must not be published. Owner source, APK, backups, DB1, V3/V7, approval and signing keys remain unchanged. The complete original-source bundle remains local/owner-only and is not uploaded to public GitHub.
