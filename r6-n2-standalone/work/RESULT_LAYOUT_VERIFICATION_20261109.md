# N2 6.21.28 expanded result screen verification — 2026-11-09
- Basis: captain screenshot 1009x2048, result table low on page with large empty top.
- Scope: ONLY result overlay; do not modify approved home / navigation / original record data.
- Local layout checks: 4/4 pass (result-specific CSS scope, top-alignment/width/scroll, guard on unknown DOM targets, JS parses).
- Existing restore and chat tests: 6/6 + 3/3 pass. These are module tests, NOT Android integration or PNG visual tests.
- Server update.json remains 6.21.27. No 6.21.28 APK signed or released.
- Blocking: GitHub's binary APK fetch returns empty content or decode error; container direct HTTP is DNS blocked. Private signing key not available in current runtime.
- Required next: get actual signed APK bytes, identify real HTML DOM element names and persistence keys, integrate on copy, verify signed same certificate, then publish binary and update.json atomically / last.
