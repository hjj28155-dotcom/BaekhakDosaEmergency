# ⚓ V13 R8.29 / R16 Stage 3 — official draw lookup correction (2026-11-01 KST)

## Operating state and protection
- Production remains **V13 R8.29 / versionCode 31337 / remote revision 15**. No APK rebuilt, signed, uploaded or installed.
- Current V13 report storage, DB-1, V3/V7 originals, approvals and locks were not overwritten or reset.
- Work is only in research branch `research/v13-r16-engine-contract-20261101`.

## Root cause
2026 DHLottery website revision made `selectPstLt645Info.do?srchLtEpsd=round` capable of returning latest draw, ignoring requested round. Current R15 first attempts that URL; if requested round is absent from returned `data.list` it falls back to a public mirror, so official validation is not achieved. Specific-round API is `/lt645/selectPstLt645InfoNew.do?srchDir=center&srchLtEpsd=<round>`. Reviewed 2026-10-22 source and other independent implementation examples.

## Changes STAGED (not published)
- `research/v13-r16-engine-contract/v13-ui-r16-official-fetch-candidate.html`, blob SHA `caa45bdbd95f81d342527e184cdb2f3eb6c2dfda`.
- URL corrected to round-specific source. Existing exact `ltEpsd` matching, six unique number and bonus checks retained.
- Added fail-closed `scoreAfterDraw` guard: records from `PUBLIC_MIRROR_UNVERIFIED` do not become ready; no official grade PNG or congratulations is emitted on mirror result alone.
- `research/v13-r16-engine-contract/v13-ui-r16-official-fetch-smoke.test.cjs` compiles inline JS via Node VM and invokes the *actual candidate* result parser / scoring guard with mock official and mirror records.
- `.github/workflows/v13-r16-research-smoke.yml` runs offline tests on this research branch only; contains NO deployment step.

## Verified evidence
- Local Node standalone official-round API contract: **14 tests pass / 0 fail**, including 1248 -> 2026-10-31 and 1249 -> 2026-11-07, mixed 10-record selection and mismatch rejection. Mocks, not a claim of actual server connectivity.
- GitHub Actions run 41253444311, SHA `e9c587877bc7ec7b53812e718348d7edcf099d2f`, **success**. Job `static-and-contract-tests` succeeded; log shows **33 tests, 33 pass, 0 fail** (10 shape, 13 official lock gate, 10 candidate HTML/script tests).
- Candidate remote HTML exists in the branch with the original R15 content and only narrow researched changes. No published manifest changed.

## Remaining risks / next priority
1. Real Android phone official DHLottery CORS / response check, restart, PNG and download check. Offline CI did not connect to the operator server.
2. True `com.baekhak.dosa.v3` owner MASTER latest signed APK/source plus current independent captain/dosa/freedom/navigator engines are still unverified. Drive V3 R16 `com.baekhak.dosa.f1` is a separate app, not the owner MASTER.
3. Independently trusted external BEFORE lock and source verification is required before calling results official. Local `sourceVerification:'UNVERIFIED'` cannot self-certify. R16 staged retrieval fix does not solve engine completion.
4. Keep original V1 popup policy: show only on actual release/notice; user initiates APK download/install. Verify same signing key and data retention before altering main `releases/v13-release.json`.

## Current next step
Trace genuine V3 owner MASTER and four independent engines, connect verified BEFORE provenance and official draw read; then Android end-to-end QA. **Do not deploy R16 simply because offline tests passed.**
