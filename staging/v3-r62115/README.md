# V3 R6.21.15 V1 update popup staging
- Candidate only. Do NOT publish to main or mark update_available=true before actual APK signer + installed package upgrade verification.
- R6.21.14 engine and 1249th-round purchase combinations remain unchanged.
- Add the JS to the native V3 WebView index.html only after checking the AndroidHost getNativeVersionCode bridge.
- For notices, set notice_id to a new token. For verified APK updates, provide HTTPS URL, exact APK SHA-256 and higher version_code.
- The current standalone test app com.baekhak.dosa.r3 is a DIFFERENT package and cannot update com.baekhak.dosa.v3.
- Test draft locally in /mnt/data/V3_R62115_V1_업데이트_팝업_개발자료_20261107.zip; actual Android integration pending.
