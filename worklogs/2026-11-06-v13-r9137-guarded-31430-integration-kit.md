# V13 R9.13.7 exact-baseline guarded application kit — 2026-11-06

## Captain instruction
- Continue latest captain-only V13 (not public-distribution, not V3/V7) work.
- Installed baseline: actual captain-submitted APK `공유`, package `com.baekhak.dosa.v13r3preview`, versionCode **31429**, R9.13.7 NAV PRIVATE CUM DB.
- Already completed R9.13.7 asset-only UI patch: `V13_R9137_31429_분석방_R25_최신자산패치_20261106.zip`. Analysis room above settings and research-only statistical guidance remain preview-stage.
- Previous AndroidIDE 31421 built APK was rejected because installed 31429 is newer; do not force downgrade, uninstall, wipe DB, or use 31421 sources.

## New work
- Verified Drive V13 root/자료실/update folders for exact **31429 native Java/Gradle project**; did not confirm an exact match. Existing R9.13 stage project is not a safe native baseline.
- Built a strict source-integration toolkit `V13_R9137_31429_TO_31430_검증형_화면이식도구_20261106.zip` (909,734 bytes, SHA-256 `63b7d72655904383e3178bf171a72f3af23dfba7dd8542e1e9703ac1ffed2c4b`).
- Contents: `patch_v13_r9137.py`, all **81 original R9.13.7 APK asset SHA-256** hashes (read-only baseline), R9.13.7-approved updated `assets/index.html`, README.
- Command without `--apply` is DRY RUN and changes zero files. Rejects any missing/modified baseline asset, wrong package, wrong versionCode, or missing Java Gradle Android project.
- `--apply` only accepts app project versionCode 31429 with the exact baseline 81 asset hashes. Creates backups of `assets/index.html` and `app/build.gradle`, swaps just HTML and versionCode **31429→31430**; rollback backup used upon write error.
- Verified in isolated synthetic project reconstructed from exact baseline APK assets: dry-run PASS; old version 31421 rejected; wrong asset rejected; positive guarded apply PASS with backup + resulting HTML SHA-256 `39f8282b1ca982bb385fe765c1c7a4188765a97db2dd18a6806b66772730d5b9`.
- **Critical limitation:** checking 81 APK assets does not establish equivalence of Java/native source to APK `classes2.dex`. The tool cannot create a safe installable APK unless the exact 31429 Java sources are independently verified. User must not run `--apply` on older projects nor install an APK rebuilt from unverified Java source.
- No current installed APK or local DB edited; no new APK built/signed or deployed.
- Toolkit uploaded to Drive as new file: https://drive.google.com/file/d/1eyfMjQuT0Pr2MXoJWG8B6gAWvIHzUmd2/view?usp=drivesdk ; metadata readback size 909734 bytes, ZIP.
- Next: source exact R9.13.7 Java/Gradle project or proof of native equivalence; then controlled integration, build, signature, package/DB preservation and handset tests.
