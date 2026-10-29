# V3 R60 → V8 client source module, 0.2.0

This is a source integration module, **not an installable or signed V3 APK**.
Base APK: `com.baekhak.dosa.f6`, versionCode `30193`, R60 SHA-256
`87ccc77af2a1bd2e5a3b19f31ef84e5e21eccffda717daf2e183c726f9f58026`.
Required signing certificate SHA-256:
`eedcfd3730f650e55defee1b0c971b77117b241211d73eb02a8fd9982245a48c`.

## Implemented

- JS adapter reads the existing R53 IndexedDB only; validates its analysis, first lock and exact report envelope before queueing.
- Android library copies exact report JSON bytes into its own durable SQLite outbox, checks SHA-256 and rejects same-round changes.
- WorkManager sends one unique job per round after connectivity returns. It does not run the research engine or promise an exact hourly execution time.
- Upload to existing V6 common storage; check upload hash; checkpoint asset ID; POST V8 receipt; authenticated GET receipt; compare both responses; only then expose VERIFIED to JS.
- JS atomically changes only centralOutbox status and adds receipt/hash metadata. Analyses, roundLocks and original payload stay unchanged.
- 401/403 -> NEEDS_AUTH, temporary network/5xx/408/429 -> RETRY, integrity conflicts or other 4xx -> HOLD. No original record is removed on failure.
- Device bearer is encrypted with Android Keystore. No setter or token accessor is exposed to JS; no service-role key is used.
- `prepare_assets.py` checks the exact base APK, prepares separate overrides, fixes hourly analysis checks to accept SENT_VERIFIED and changes future draw queries to V8. It never modifies the base APK, existing stored draw records, or engine_v2.html.

## Host integration required

The recovered R60 source ZIP contains APK resources and a manifest-repair script, **not the full native Android project**. Do not replace its MainActivity with an unrelated V4 or V8 host.

1. Recover the R60 native project and its build configuration. Include `android` as a Gradle library project. Keep package `com.baekhak.dosa.f6`, WebView data directory/origin, existing DB names and current application class/WorkManager initialization behavior. Use versionCode greater than 30193 only after checking installed/latest release versions.
2. Bind `new kr.hanghae.v3v8.V3V8Native(context)` as `V3V8Native` **only** in the local asset WebView. Remote links must open externally; never expose this bridge to remote pages or untrusted frames. Verify existing local engine iframe behavior.
3. Use `prepare_assets.py R60.apk NEW_DIRECTORY` and review/copy its three overrides. Its source loader reconciles pending reports every 15 seconds while the page is alive. The Android worker handles already-queued reports after page closure. First-lock-to-native-copy gaps recover when the original WebView is reopened; instantaneous background handoff from an already-closed old R60 is impossible.
4. Implement trusted native enrollment against a dedicated, revocable V3 device record in existing authentication infrastructure. Call `TokenVault.provision(context, token)` from native enrollment only. Never accept a token from a web page, commit it, or put it in an asset. Exclude auth preferences from host backup; re-enroll after key loss. Enrollment is not implemented by this module.
5. Build the **original host** using its existing signer. Verify certificate above, Android manifest, resources and data preservation before offering an APK for installation. This module's AAR is not an APK and cannot update R60.
6. Test on device: first-lock report -> native durable queue -> upload -> V8 POST/GET receipt -> original outbox SENT_VERIFIED; network interruption, process death, authorization revocation, retry without duplicates, and first-lock preservation.

## Verification

`tests/adapter.spec.cjs` uses Chromium's real IndexedDB with the original R60 outbox module and a mocked native bridge. It checks original payload preservation, invalid receipt refusal, idempotence and retries. It does **not** prove a device or live server upload.

CI `verify-v3-r60-v8-client.yml` compiles/lints the Android library and runs browser tests. A green library build does not prove integration into the native host, signing, enrollment or phone installation.

## Still blocked

Full R60 native Android project; accessible original signing key and unlock mechanism; dedicated V3 enrollment; original-host APK build; actual phone/server integration test. Existing V7 services/data are untouched. V7 draw service remains available for old installed R60 until its replacement is verified.
