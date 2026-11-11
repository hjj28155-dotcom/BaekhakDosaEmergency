# N2 R6.21.46 update-channel safety gate — 2026-11-11

Captain requests V1-style in-app upgrade: startup popup on new version, user presses download and manually installs; never delete old app/data.

Facts verified:
- Owner pilot APK `N2_R62146_FINAL_OWNER_TEST.apk`, bytes 2387483, SHA256 `9206dba864861277df414e5b6fa44e478af68d6349b95c6ac8c731a78f4b3785`. ZIP CRC passes; AndroidManifest.xml and assets/index.html present.
- Uploaded to Google Drive and fetched: https://drive.google.com/file/d/1DAovhurYkt0vmDIro8V32VY8eqxW9Y4O/view (file ID confirmed).
- Official GitHub `r6-n2-standalone/update.json` currently version_code 621941 version_name 6.21.41, APK hosted on public raw.githubusercontent.com branch.
- Installed source R6.21.46 contains `NativeUpdate.check` at app launch, `NativeUpdate.download`, `NativeUpdate.install`, and expects official manifest URL on `main`.

**BLOCKER:** Current 62146 binary is on account-scoped Drive file, not verified to be anonymously downloadable as raw APK from the native updater. Do not change official update.json to Drive view link, which may return HTML/redirect and cause corrupted update. Need a validated public direct HTTPS APK hosting URL with SHA-256 verified, then update official manifest atomically and test installed R6.21.45 to 62146. No update manifest changed, no app auto popup claimed. Official R6.21.41 remains published. Existing app & DB preserved.
