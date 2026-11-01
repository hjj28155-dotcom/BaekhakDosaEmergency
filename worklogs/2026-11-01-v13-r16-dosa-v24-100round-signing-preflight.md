# V13 R16 research QA — 2026-11-01

- Original DOSA V2.4-RC archived ZIP source is byte-identical to the code bundled in V13 R8.29 (SHA256 90f6295b756968aa81f8f8e642113d74812c247863ff289f41773561e317c72e).
- Actual V13 DB1 RAW for historical rounds 1-100 produces 500 valid combinations with no within-round duplicates, no nondeterminism, and no execution errors.
- Original engine lock says DEVELOPMENT_BASELINE_NOT_50PCT_PASSED; range 1-100 only. It must NOT be used as the official current-round DOSA engine.
- An isolated fail-closed adapter passed 11 local Node tests, including rejection of rounds 101, 1248, 1249 and official BEFORE registration.
- A local, password-prompted P12 certificate/owner-key checker passed 6 local tests with synthetic keys and the actual V13 R8.29 APK certificate. Actual owner's encrypted P12 was not opened; signer identity not yet verified.
- Owner archives, APK, P12, DB1 and production main branch remain unchanged. No official app build, install or release claimed.
