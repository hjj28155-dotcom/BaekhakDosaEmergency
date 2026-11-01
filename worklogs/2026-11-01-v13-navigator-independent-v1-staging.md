# 2026-11-01 V13 Navigator independent engine V1 — staging

Captain instruction: Preserve V12 integrated captain/dosa/freedom engines. Build and add a NEW separate navigator engine only to navigator room.

Implemented navigator_independent_v1.js with independent deterministic 6-text-sentence weighted selection. No V12 captain/dosa/freedom code imported. It generates 5 distinct sorted 6/45 combinations, uses round 1..9999 and original RAW6 fingerprint, rejects invalid input. sourceId NAVIGATOR_INDEPENDENT_V1_RESEARCH_20261101, researchOnly true, officialLocked false.

Integrated as an additional script at the end of app/src/main/assets/index.html, after legacy research registration, using window.V13_RESULT_BEFORE.registerEngine('navigator',generate). Preserved 67 transplanted V12 Freedom assets, other V12 engine files and original V3 directory unchanged.

Node --check and deterministic/valid five sets/unique/RAW sensitivity/1,1248,1249,9999/validation/research flag unit harness passed.

Staging ZIP V13_V12_삼엔진보존_항해사독립엔진V1_미배포검증본_20261101.zip: 93,994,679 bytes, SHA256 7d7e2ca1c3d444910fd182630ddf06962500f9d1c79c990f9bfb55061cf2e88a, 206 entries, ZIP CRC pass.

IMPORTANT: This is an R8.15-derived source staging project, not latest revision15 merged and NOT a signed/installable APK. The V13 live rev15 native source may use stricter API/count validation. Independent algorithm is new research, not verified prospective original or actual user-submitted before-draw proof; do not mark official or assume accepted PNG. Device runtime and storage tested? NO.

Unmodified: GitHub release UI rev15, APK, DB-1, previous reports, official draw and approval locks. Next: merge into latest v13 revision15 with 5+5+15+5 contract and verify real WebView output; V12 3 engines integration remains incomplete.