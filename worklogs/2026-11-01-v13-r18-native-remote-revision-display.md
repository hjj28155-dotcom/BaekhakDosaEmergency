# ⚓ V13 remote UI R18 — native UI revision visibility (2026-11-01)

## Captain request / prior screenshot
Installed APK V13 R8.29 (31337) said 최신 버전입니다 but did not show remote UI 17 revision. Captain required remote UI revision visibility.

## Root cause and native proof
The UI update screen previously displayed only APK versions; native R8.29 APK classes2.dex exposes getUiRevision(): int and checkForUiUpdates(): void.

## Delivered fix
- Remote revision 18 needed so devices already at R17 can retrieve changed HTML; original R17 preserved.
- Published releases/ui/v13-ui-r18.html, Git blob 69a8098ebc13a05505d16609b544585764a9c80b.
- Source SHA-256 8138d1bcd4e68e9f89b015b84ca5c6072373fd51baf63c91684751d26849256b; UTF-8 bytes 1,621,587.
- Manifest now revision=18, deploymentMode=remote_ui_opt_in; V13 R8.29 (31337) APK unchanged.
- Displays current remote HTML 18, native getUiRevision() recorded revision, and visible R17 feature preservation: four-room analysis state, score table, win/loss, fanfare.
- Provides distinct 원격 UI 업데이트 확인 and APK 업데이트 확인 buttons via native checkForUiUpdates() and checkForUpdates(false).
- No verification of real device application yet; missing native revision is marked 확인 불가.

## Verification
- 2 inline JavaScript scripts syntactically checked; 15 static guards passed.
- Native revision simulations: 17 differs from document 18; 18 matches; invalid/no bridge treated unverified.
- Source SHA256 algorithm crosschecked against R17 known digest/size.
- 9/9 GitHub re-read post-publish checks pass, old R17 blob unchanged.
- No signed APK build, handset execution test, or change to DB-1, old records, original engines or V3/V7.

## Handset validation
Reopen V13 > accept remote UI 18 > update screen. Expect current open remote UI 18, native device applied 18, and R17 features included. If there is no remote prompt, tap 원격 UI 업데이트 확인. APK latest message may still show R8.29 and is normal.
