# LOTTO645 V75325 — independent four-room engine full replay (2026-11-22)

- No changes to APK or production update metadata. Original V75320 vs V75325 `assets/index.html` SHA256 identical: d59da48ae16c3ac809c9258ad75b2433b11da9b72e90eae99d18bbc4c8c61ee6.
- 1,251 rounds (1..1251), 25,020 ticket outputs; four rooms maintain distinct top-12 candidate sets every round; zero intra-round duplicate 6-number combinations.
- Tested using ONE mock device seed; draw history 1..1250 with round 951 test-only reconstructed; four batches ~56.93s total. Full replay completed PASS (previous timeout resolved with batch tests).
- Research archive: https://drive.google.com/file/d/1i2WTlWFrVrOIeiR1Sw749bDNzCGHxpuB/view
- **RELEASE HOLD:** pre-30-round UI logic not updated (original UI immutable); real-device overwrite/data integrity untested; no cross-device zero-duplicate central reservation in V75325 APK; round951 missing in shipped baseline; no win-rate superiority established.
- Production main release.json continues version 75313; no user phone reset or data changed.
