# LOTTO645 V75322 Stage 6 — isolated replay ledger (2026-11-22)

- Actual stage-5 screen storage constants inspected. Five legacy draw, locked tickets, receipts, official results, grading keys retained.
- Separate staging replay namespace with SHA-256 legacy snapshot check, no deletion or overwrite of old ledger.
- Only 20 authenticated central reservations / 4 rooms x 5 / matching device and round can be committed to new staging ledger. Repeated round write rejected.
- Node.js 9 checks PASS; no real device or actual account authentication tested.
- Artifact: https://drive.google.com/file/d/1pTlZ7YX2uRUfYSGdi2zzOPpx3JgCmSDu/view
- NO-GO: no signed V75322 APK; no live install/reset; main release remains V75313.
