# LOTTO645 V75322 Stage 8 — backup + server-auth gate (2026-11-22)

- Inherited Stage 7 staged screen and isolated replay ledger.
- Added legacy-5-key JSON export UI with SHA-256 digest and required human file-confirmation before isolated ledger activation.
- Added Supabase `/auth/v1/user` token validation before accepting externally provisioned session (credential issuance/login UI not implemented).
- Stage8 Node tests 17 PASS; Stage7 regression 11 PASS; 38 HTML inline-script syntax checks PASS.
- No stored user purchase/grade data cleared. Download confirmation is human-operated; Android WebView file delivery untested.
- Study archive: https://drive.google.com/file/d/1uXDjoGBfVW4-S6FxprhrvTchVRasQmoX/view
- NO-GO for production: no signed APK, no actual phone restore tests, no full 1-1251 live reservation; keep production release 75313 unchanged.
