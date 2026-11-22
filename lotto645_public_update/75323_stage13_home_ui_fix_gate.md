# LOTTO645 V75323 Stage13 home screen cleanup (2026-11-22)

- User photographed oversized V75322 backup + OTP sections. Stage13 makes them collapsed inside a small optional test-settings disclosure, preserves all original app menus and storage safeguards; improves button contrast.
- APK package com.hanghae.lotto645, versionCode 75323, cert SHA256 94d01930adc6b370c487a437e03edf9d37c4dd8ec695e65979e1023d5e37711b.
- Static ZIP PASS, v2 signed digest PASS, 138 ZIP entries maintained, only AndroidManifest.xml and two UI JS assets changed, Node syntax checks PASS.
- APK SHA256 3f623f898d5fdd955c1dd78caf4bc12fbb6c7d555c81a871129462fcf9ef216f
- Test APK: https://drive.google.com/file/d/1ZHbr69QiytuU84cOukTKRjku655YpLtW/view
- NOT production-safe: hidden panels still necessary for backup/auth, no Android actual UI or login test, no live central allocation verified, device existing records may be affected by testing. NO-GO to automatic rollout.
- Existing production release.json remains V75313; original apps/devices untouched.
