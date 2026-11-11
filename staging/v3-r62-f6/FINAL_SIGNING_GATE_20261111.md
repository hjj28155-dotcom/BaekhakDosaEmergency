# V3 F6 R62 final release gate — 2026-11-11

## Target
- Package: `com.baekhak.dosa.f6`; candidate versionCode 30195.
- Historical signed R60 (30193) cert SHA-256: `eedcfd3730f650e55defee1b0c971b77117b241211d73eb02a8fd9982245a48c`.
- Prepared structural APK SHA-256: `11ddc0096b4f86b1c152706956c8f533b2457aaf915f2767197435c4d6c31e89`; **UNSIGNED, DO NOT INSTALL**.
- Stored build package: https://drive.google.com/file/d/14DpNr6jFEuuLX74zHYJfAtyD7-zZ-CdU/view

## Actual verification performed
- Android `keytool -list -v` opened two locally available backed-up stores with the documented credential:
  - `freedom_v4_release.keystore`: cert SHA-256 `03bc4f776ba3a93feb91b355a50b164209e20163fbb73eda0b7a8b7fd2273366` -> **NOT target**.
  - `V33_동일서명_원본_보호키저장소.p12`: same SHA-256 `03bc4f776ba3a93feb91b355a50b164209e20163fbb73eda0b7a8b7fd2273366` -> **NOT target**.
  - `V3_R33_f2_signing_encrypted.p12`: present, but cannot be opened using documented credential; certificate fingerprint and signing eligibility **unverified**.
- Do not sign V3 F6 with the V4 key or other package's signer.
- A source candidate is not a tested installable signed APK. No real-device overwrite install or data-retention test performed.
- Existing live release not modified. `main/v3-f6/update.json` is isolated and **update_available=false**.

## Remaining mandatory gates
1. Identify the authorized F6 private-key store matching historical certificate SHA-256, unlock locally (never print passwords), verify PrivateKeyEntry.
2. Build correctly zipaligned APK with versionCode greater than installed version and no unrelated asset changes.
3. Sign APK v1/v2 with matching certificate; independently verify APK signature and SHA-256.
4. Validate update-install on the captain's installed V3 app, preserve DB and check popup/download flow.
5. Upload verified signed APK and set manifest to update_available=true with exact HTTPS URL and SHA-256 only after above passes.

**Status: BLOCKED BY MATCHING SIGNER; DO NOT PUBLISH UNSIGNED APK.**
