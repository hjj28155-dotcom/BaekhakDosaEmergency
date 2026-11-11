# V3 R6.21.15 V1 update popup staging
- Candidate only. Do NOT publish to main or mark update_available=true before actual APK signer + installed package upgrade verification.
- R6.21.14 engine and 1249th-round purchase combinations remain unchanged.
- Add the JS to the native V3 WebView index.html only after checking the AndroidHost getNativeVersionCode bridge.
- For notices, set notice_id to a new token. For verified APK updates, provide HTTPS URL, exact APK SHA-256 and higher version_code.
- The current standalone test app com.baekhak.dosa.r3 is a DIFFERENT package and cannot update com.baekhak.dosa.v3.
- Test draft locally in /mnt/data/V3_R62115_V1_업데이트_팝업_개발자료_20261107.zip; actual Android integration pending.

## 2026-11-11 Drive cross-check (no APK integration)
- Verified Drive folder: 항해사앱_V3 / V3_R62_홈누적DB_서명대기_20261030.
- R62 VERIFICATION.json explicitly says UNSIGNED_NOT_INSTALLABLE, package com.baekhak.dosa.f6, versionCode 30195. Its listed Android same-signature signing/install tests remain undone.
- This staged popup targets DIFFERENT package com.baekhak.dosa.v3. Never merge these candidates or publish an update without identifying the exact installed app/package, latest native base, signer and bridge method.
- The N2 standalone app com.hanghae.fold8n2app has a separate 2026-11-10 R6.21.35 success handover. Do not treat N2 APK or signing key as V3.
- This staging popup's notice-read storage fix was committed separately on 2026-11-11. No release feed, Android APK, or production database was modified.
- NEXT: locate current installed V3 production APK/source and verify package/signing certificate + AndroidHost.getNativeVersionCode before native integration and real device regression tests.
