# V13 V12 Freedom full-app transplant, staging only — 2026-11-01

User explicitly instructed: remove all engine round ceilings, and transplant V12 Freedom app as-is into V13. Preserve all source and DB/approval records.

Drive source located: V12_R9_독립Android프로젝트_20261030.zip, 18,171,428 bytes, 75 entries and ZIP CRC pass. This actual V12 R9 independent Android source (com.baekhak.dosa.v12renew) contains 67 assets including index.html (234,975 bytes), engine_v2.html (8,734,078 bytes), raw_bank.js, engine modules, references. README expressly says PRE-COMPILE SOURCE DRAFT, native parity and certificate/signing not verified. This should NOT be confused with V3 R62 embedded app.

Local staging ZIP built: V13_V12_항행의자유_원본이식_미배포검증본_20261101.zip, 93,990,537 bytes, SHA256 454f80f984077f2022262c0e5632b4b0bdddc72a50af598f70a8413c1a7e8f17, ZIP CRC pass.

Transplant details: preserve V13 R8.15 project baseline; add all 67 original V12 assets unchanged under app/src/main/assets/v12_freedom_original; change V13 base index.html only in view===9 from V3 R62 iframe to v12_freedom_original/index.html. Original V3 folder remains unchanged, as do DB/engines/approvals. No V12-to-V13 score or lock data migration; source distinction is visible.

Important: latest deployed V13 remote UI rev15 is newer than source baseline R8.15. The staging ZIP is NOT a complete installable latest V13 app; still requires careful diff/merge, WebView and native integration, tests and build. No GitHub manifest change, new UI revision, APK build, signing or device test.

Engine range ceiling request: scoped separately. Existing experimental candidate patch located; do not certify 1249 official 30 combinations by disabling source authority, valid-six, original packGate or draw locks. Actual engine capability and official source still need verification.