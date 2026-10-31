# V13 R8.29 / remote revision 15 — R16 research stage 2, 2026-11-01 KST

## Scope and immutability
Research branch only: research/v13-r16-engine-contract-20261101. No manifest, production UI revision, APK, approval, key, lock, DB-1, archived BEFORE or cumulative records changed.

## New verified source facts
- V3 R16 source archive from Drive (9,387,507 bytes; ZIP/inner assets CRC PASS) is **NOT** true V3 OWNER MASTER. Its own README says separate Android package com.baekhak.dosa.f1, versionCode 30160, and no migration to installed V3.
- V3 0.85 metadata confirms genuine master candidate com.baekhak.dosa.v3 and changed versionCode 30081→30085, but accessible Drive MASTER R4 tree has only folders and no verified current binary/engine source in this audit.
- GitHub private BaekhakCrownAndroid tree has no matching owner V3 master engine code. Missing originals remain missing.
- Actual V13 R8.29 APK contains V1-style native JS bridge callbacks, update popup and acknowledgement UI; release manifest is versionCode 31337, same as APK, announcement:null, so no newly available APK or notice is advertised at this time.
- Remote r15 source function scoreAfterDraw checks if record exists and collects 30 sets, but does not require record.verifiedOfficial===true to make ready=true. Public mirror is explicitly UNVERIFIED; therefore grade/celebration is not eligible for official reporting based on mirror data.
- Local user-imported BEFORE records carry sourceVerification UNVERIFIED and officialLocked false. SourceId text alone is not independent provenance attestation.

## Candidate changes (NOT DEPLOYED)
- Existing v13-independent-engine-contract.cjs from stage1: 5+5+15+5 format checks, 10/10 tests.
- New v13-official-score-gate.cjs: blocks score unless an independent trusted official-draw verifier *and* all four independently verified immutable pre-draw locks are supplied. Rejects invalid draw, duplicate sets, malformed groups, bad hashes and post-cutoff locks; never mutates storage.
- Local Node gate tests: 13/13 PASS. Mock attestation in tests is a fixture only; it is **not** an operational server verification and it does not establish official eligibility for any real draw.
- Production code is not patched because trusted source verifier + pre-draw lock are not yet connected, APK signer equivalence and Android device QA not done.

## Next priority
1. Locate V3 MASTER owner package com.baekhak.dosa.v3 signed source/apk and trusted captain/freedom current engines, compare hashes and actual exported functions without mixing V33/R62.
2. Wire trusted BEFORE lock and official draw verifier; demonstrate genuine future pre-draw 30 sets; do not retrospectively invent round 1249 BEFORE.
3. Apply revision-16 UI patch only after end-to-end engine and official scoring QA; ensure 2026-11-01 same-code manifest cannot trigger 'new APK' prompt.
4. Android device test 4 PNGs, combined PNG, automatic official 6+bonus, cumulative DB preservation, startup V1 update notice, same-signature APK install.
