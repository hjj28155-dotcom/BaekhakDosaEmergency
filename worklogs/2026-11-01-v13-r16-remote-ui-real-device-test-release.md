# ⚓ V13 R8.29 (31337) · Remote UI revision 16 published for real-device test
Date: 2026-11-01 Asia/Seoul

## Captain instruction
Update the existing installed app so the new independent Navigator V1 screen can be seen and verified on the actual phone. Preserve original software and records.

## Live release
- Repo: `hjj28155-dotcom/BaekhakDosaEmergency`.
- Published HTML: `releases/ui/v13-ui-r16.html`, Git blob `6267fe826baa840241d32e5cae13f724bb510613`.
- UTF-8 bytes: 1,616,288; SHA-256: `dec5d79b811239d2eadc278b97895abb1c6bdd6ea0751c02ec91f198ff8edaff`.
- Original rev15 UTF-8 SHA-256: `e7a0cc928dea4bbadd762ed3d827a678d16a0227186baf07537d22b19bcbb648`. The same independent pure JS SHA-256 routine matched that published baseline exactly.
- Release manifest `releases/v13-release.json`: `uiUpdate.revision=16`, `minVersionCode=31337`, `deploymentMode=remote_ui_opt_in`, manifest commit `a0ee435c3fedfa37cd64acb3ae7803f62fce3d4a`.
- The APK's package, latestVersionCode `31337`, APK download URL, existing APK bytes, DB, and original engines remain unchanged.
- Existing APK stored in Drive: `V13_R8_29_REMOTE_UI_31337_20261031.apk`, file ID `1zBa-KXjceUxj7gbwUp_RTHTCadVoTm0S`, 15,032,713 bytes; independently inspected APK ZIP, Java/Dex strings confirmed remote UI update methods and trusted rev filename pattern.

## Revision 16 behavior
- Navigator V1 synthetic RAW6 5 independent combinations are isolated research preview, auto-run with RAW6 analysis and visible in Navigator room with a manual repeat button.
- First-report import now rejects research, legacy or explicitly ineligible outputs. Existing saved research entries remain unmodified but are excluded from valid-room receipt PNG.
- Formal 5+5+15+5 counting, official draw-only grading, and DB-1 were preserved in static checks.
- Source patch recorded in draft PR #7 and separate research branch, not merged.

## Completed tests
- JavaScript syntax check for 2 inline scripts; 4/4 round-specific output-format/determinism checks (1, 1248, 1249, 9999) with synthetic RAW6.
- 3/3 research import blocking tests and 7/7 static regression feature checks.
- Isolated navigator function execution with sample round 1249: no localStorage writes, invalidated cached memory upon changed RAW6, rejected missing RAW6.
- Post-publish direct GitHub readback verified `r16.html` Git blob, byte size, manifest revision, SHA-256 string, opt-in mode, minVersionCode and unchanged versionCode/APK URL.

## Pending physical-device test — NOT YET VERIFIED
- Open installed V13 R8.29 app, accept new Remote UI 16 prompt if presented (or use in-app update check); confirm actual updated UI.
- Open menu > 원문 분석 > select a DB-1 valid round (e.g. 1248), run 원문 분석 실행, then 항해사방 and inspect 5 research sets and labels.
- Verify existing official reports, locks, DB-1 and local records unchanged, and confirm no research results entered formal 30-combination ledger. Inspect display/scrolling on folded and unfolded phone.
- Browser DOM/WebView runtime and exact round 1249 DB-1 originals remain untested. Captain's phone screenshot/feedback is required to confirm real on-device rendering.
- Other V12 original three engines still not officially connected for formal 30. No new signed APK built or installed.

## Rollback
- Preserve `releases/ui/v13-ui-r15.html` unchanged and prior manifest SHA/blob `0cb5a999866360fcc9f536700a8ea06a9a6e569c`. If a problem is reported, restore revision 15 metadata after investigating cached client update semantics; do not delete original data or history. Physical client cached UI may require explicit user action.
