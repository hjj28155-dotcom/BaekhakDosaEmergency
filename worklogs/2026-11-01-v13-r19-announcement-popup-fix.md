# V13 R19 remote announcement popup fix — 2026-11-01 KST

User symptom: no popup on installed phone after remote UI r19 publish.

Root cause verified in the actual R8.29 APK classes2.dex and HTML:
- Native Java `ExitBridge.checkForUpdates()` parses the release manifest's `announcement` with `JSONObject.optJSONObject("announcement")`; a plain JSON string is discarded.
- The bundled V13 R8.29 UI's `V13UpdateResult` calls `v13ShowPublishedNotice(result.announcement)` only when the status is `latest`; the handler requires an object with valid `id`, `title` and `message`.
- The live manifest at commit `f496221...` had a **string** announcement. The missing notice was thus caused by an incompatible manifest schema, not by a missing APK update.
- Remote UI automatic popup is a different native path (`MainActivity.onResume -> checkForRemoteUiUpdates`). The APK does support it but no physical phone diagnostic data is available.

Correction on PRODUCTION main:
- Updated only `releases/v13-release.json` `announcement` to {id,title,message} with stable ID `v13-r19-remote-notice-20261101`.
- Commit `d9f40c833d534dadbbe8b0c55969a79e8723a157`; manifest blob SHA `1644d730ca14991353008cc03ed4613c6b458c29`, re-read and verified.
- Preserved APK versionCode **31337**, remote revision **19**, remote UI SHA256 `4470b0d5b1308f7fb72a114cbf7ea20eee9f452738e3c1e461fb8f0fd7f51619`. No APK rebuild, database write, or other release file change.
- Message instructs user to tap update → remote UI update check → apply; never suggests reinstalling or downloading a new APK.

Executed browser smoke using actual bundled R8.29 APK index.html + all five original linked scripts inlined (no fake UI), in-memory localStorage shim for about:blank browser tests:
- At viewport 393x854 and 320x700, old string does **not** display popup.
- New typed object displays notice titled `⚓ V13 원격 19회차 업데이트 안내`.
- Acknowledge closes notice; repeat check with same ID does not reopen.
- No JavaScript page errors.
- Screenshot `notice_393x854.png` local only; this is a simulated bridge callback, **not an actual device test**.

Next: user reopens installed V13 R8.29 while online or taps `APK 업데이트 확인` (only to retrieve the manifest and notice), then the separate `원격 UI 업데이트 확인` button to apply r19. Native remote UI check may fail for other reasons (network, phone version), so don't claim device success until confirmed.
