# LOTTO645 V75325 — four independent candidate rooms, original home retained

- Captain cancelled shared 12-number pool. Each of 4 rooms now independently ranks its candidate 10/12 numbers; partial number overlaps permitted.
- Based on V75320 original app. Changed **only** AndroidManifest.xml (version 75325) and assets/LOTTO645_R161J_UNIQUE_auto.js. `assets/index.html` byte-for-byte unchanged SHA256 `d59da48ae16c3ac809c9258ad75b2433b11da9b72e90eae99d18bbc4c8c61ee6`.
- Unit test sampled 17 historical rounds and 340 combinations PASS; 20 unique per round; 4 room candidate sets differ in samples. Full 1251-run timed out, NOT verified.
- Owner-signed APK static v2 digest PASS, APK SHA256 `591c35e529c858b0e3d301d73a5082f5c6a963339893bb81c2c2c71b126def95`.
- Test APK: https://drive.google.com/file/d/1PJXmY-TzBc49ekpa1CO3cPN4gaCCpKWu/view
- Engine and test sources: https://drive.google.com/file/d/1MRWVkZz2MTmIzAasiKo3rAnligtAc68a/view
- **NO-GO:** existing UI has round 1-30 30-history restriction; 951 record missing in baseline; no phone-to-phone central reservation or live install/DB preservation verification. DO NOT deploy broadly, factory reset or delete existing records.
- Main production update remains V75313.
