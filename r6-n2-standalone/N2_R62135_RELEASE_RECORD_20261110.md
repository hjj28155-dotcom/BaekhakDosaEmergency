# ⚓ N2 6.21.35 V1 update release registration — 2026-11-10

## Target
- Package: `com.hanghae.fold8n2app` (N2 only; original R6 `com.hanghae.fold8media` unchanged)
- Version: `6.21.35`, versionCode `621935`
- Source base: N2 6.21.34
- Change: add DB-1 source-only rounds 1250, 1251 six sentences each; keep 1~1249 archive, existing UI, storage paths.

## Signed APK checks
- SHA-256: `38d7d4a4e5d991526eb29dd86be56026abd37bd3d9dc4d4d686e67b67730e40d`
- Size: 2,280,637 bytes
- Signer cert SHA-256: `81d75da8b4e1c82fbe8af27a0f8512236f2b1c2cdef5d7d015f695fb98893d04`
- Official Android apksigner v2 (minSdk24): PASS in preceding build test
- CRC PASS; JavaScript syntax 32/32 PASS; browser 1250/1251 round select PASS; old 1249 sources unchanged.
- Binary Git blob SHA: `2571a10d076b02c7391254c2334a2a166227c1a2`, exactly equal to local `git hash-object`.
- Release branch: `n2-r62135-publish-20261110`.
- Raw binary URL: https://raw.githubusercontent.com/hjj28155-dotcom/BaekhakDosaEmergency/n2-r62135-publish-20261110/r6-n2-standalone/publish-621935/N2_R62135_DB1_1250_1251_SIGNED.apk

## Live V1 updater registration
- Main: `r6-n2-standalone/update.json`, changed from 621929 -> 621935
- Commit: `20ff3937ef90217b8a24f7da426032c531839000`
- Post-write re-read: package/version/hash/download URL and Git binary blob SHA matched expected.
- Original R6 channel, Lotto645, V3/V7 unaffected.

## Verification pending on captain's handset
- Public raw HTTPS end-to-end response not independently retested, device update dialog and backup preservation NOT TESTED.
- Captain explicitly approved registering now and testing on own phone.
- On phone: back up data; launch N2; confirm update popup; download APK; choose in-place Update/Install only; never uninstall or clear data; reopen; test original records, 1249, 1250, 1251 and 136 storage.
- Do not report real-device success until captain confirms.
- Signing keys/passwords remain private and excluded from GitHub.
