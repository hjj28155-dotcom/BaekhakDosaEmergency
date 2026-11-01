# V13 R8.29 rev15 + navigator V1 integration audit — 2026-11-01

## Source and work performed
- Re-read actual GitHub release rev15 HTML (blob 73fdf1add6472ce55a8ac76939df81e58d084ea9); compared with previous R8.15 standalone AndroidIDE ZIP staging.
- Initial staging merge releases/staging/v13-ui-r15-navigator-v1-research-candidate.html (commit 836a4dd3a07205d5a3a0a2f294d76b5dd6c8513d) put navigator V1 into existing V13 registerEngine path.
- Found unsafe behavior: V13 runConnectedPredraw imports ALL returned engine reports, including researchOnly, into immutable PREDRAW_STORAGE. This could reserve navigator slot and block future real predraw records.
- Corrected safe staging file: releases/staging/v13-ui-r15-navigator-v1-isolated-research-safe.html, blob 43715a2ba74fa2ab0e1d6d782928c1c93f3933fc. Keep navigator V1 as isolated research API only (V13_NAVIGATOR_INDEPENDENT_V1.generate). DO NOT register engine to official collector until research separation is explicit and validated.
- Regression static check: rev15 full-screen class and cumulative read-only view retained, new navigator V1 API present exactly once, unsafe registerEngine('navigator',generate) absent, actual release manifest still rev15.
- Previous local navigator V1 unit test via node passed deterministic output, five unique valid six-number sets, RAW sensitivity and round 1/1248/1249/9999.
- Production 3 V12 engines still NOT live-connected, no official 30-combination PNG or device test. No APK/release/DB/original source modified. Staging only.

## Next
- Implement separate in-app Navigator research preview UI with no original predraw storage writes; verify browser execution.
- Verify three authentic V12 engine invocation contracts and connect only after source identity and output are confirmed.
- Later merge rev15 + V12 original asset transplant into latest full Android project, assemble/debug/sign/device test.
