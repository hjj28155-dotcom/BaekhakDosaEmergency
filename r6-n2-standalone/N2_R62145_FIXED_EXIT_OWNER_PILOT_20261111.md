# N2 R6.21.45 owner-only test (2026-11-11)

Issue: R6.21.44 exit button reported missing on physical phone. Existing exit was placed within home DOM and subject to page clipping/overflow.

Fix: top-level fixed red-gold `⏻ 종료` button positioned at safe-area-aware top right, independent from scrollable home, separate top-level exit confirmation modal, bridge to Android.exitApp on confirmation. Display only while home visible. Preserve six compact menu buttons and marine background and all existing DB and original engine behavior.

Verified: Chromium 360x780, 430x900, 768x900: button visibly inside viewport, click opens modal, cancel works, hides on leaving home, returns on home, 6 menu button heights 56px, background data URI visible, 0 JS exceptions. Signed APK versionName 6.21.45 versionCode 621945 using identical owner certificate SHA256 81d75da8b4e1c82fbe8af27a0f8512236f2b1c2cdef5d7d015f695fb98893d04; v2 signature structural verification passed. APK SHA256 10e699621ac9c3fcce999283edaf84edf50180d5365e5a353a02422989ef48b6, size 2386022 bytes. Only assets/index.html and AndroidManifest.xml changed relative to R6.21.44 source APK.

Google Drive upload confirmed: https://drive.google.com/file/d/1y5mb8Gzwr9X-ZyydUyQ7UlXHok0cXS4I/view

Not yet validated: real-device install, Android exit action, persistence across install. No official manifest update, no published release. DO NOT DELETE installed app or DB. Official R6.21.41 unchanged.
