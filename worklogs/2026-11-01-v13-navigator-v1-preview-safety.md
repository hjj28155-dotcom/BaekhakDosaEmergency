# V13 R8.29 rev15 · navigator V1 safety preview (2026-11-01)

## Branch and code
- Branch: `research/v13-r15-navigator-v1-safe-preview-20261101` from main commit `5ae8cb0fb5578c1ab1ab4be966448e0ba75c0f17`
- Changed source: `releases/staging/v13-ui-r15-score-30-navigator-safe-candidate.html` (previous Git blob `e141fd2bfe5f498e70aa48c999ec45cb4dc714b0`).
- Source edit commit: `ec5ced1a0b9a271c85d75d8ba70f1ff13aaf9cf1`.
- This is staging only. Published rev15, Android APK, original V12 engines, DB-1, result-before local storage and user's installed app were not modified.

## Changes
1. Navigator independent V1 research executes on the same RAW6 analysis click but remains in volatile memory, isolated from 5+5+15+5 official records.
2. Navigator room shows five independent research combinations and supports repeat research generation; it labels outputs unofficial.
3. Report collector rejects researchOnly, officialEligible=false and named RESEARCH/LEGACY/UNVERIFIED sources before writing immutable result-before records, protecting future authentic submissions.
4. Old research reports remain preserved in storage and are excluded from authenticated room PNG view; no deletion/migration.
5. V12 source code and all other original room interfaces are unchanged.

## Checks carried out
- Inline JS syntax parsed: 2 scripts.
- Navigator deterministic valid 5x6/45 for rounds 1, 1248, 1249, 9999 using synthetic RAW6: 4/4.
- Research rejection import guard: 3/3.
- Static preserved-feature guards: 7/7.
- UI browser DOM, actual WebView, APK build/sign/install, production V12 3-engine wiring, official lock provenance and exact RAW6 of round 1249: not tested.

## Next highest-priority work
Verify executable V12 captain/dosa/freedom engine contracts and source fingerprints; connect genuine 5+5+15 with independently verified 5 Navigator via a single RAW6 action without allowing research data into the formal 30 or overwriting old records. Then test browser/WebView and only after verification prepare a new APK.

## Additional isolated runtime validation (2026-11-01)
- Executed the exact staged `runNavigatorResearchPreview` and `navigatorResearchPreviewFor` functions with the staged Navigator V1 implementation in a V8 sandbox, using synthetic round 1249 RAW6.
- Results: five valid sets generated; storage write spy = 0; mutating RAW6 invalidated the in-memory fingerprint; missing DB-1 round 1250 correctly rejected. All tested conditions passed.
- This is a JavaScript function-level test only, not browser DOM/WebView, APK or real official round 1249 input verification.
