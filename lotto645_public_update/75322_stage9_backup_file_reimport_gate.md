# LOTTO645 V75322 stage 9: backup file read-back gate (2026-11-22)

- Based on stage 8 saved HTML / JavaScript, preserving prior files in a separate staged copy.
- User must re-select the exported JSON, verify correct package and schema, 5 legacy keys, SHA-256 equality with both backup payload and live original storage, before enabling separate replay ledger.
- Node.js 15 tests PASS; 38 HTML inline scripts syntax verified; legacy key values unchanged in fixture.
- Archive: https://drive.google.com/file/d/1d1-2K-IU5IxNTmydZicb9MHge5IgEl0-/view
- Limitations: 5 known keys are not full Android backup; actual Android WebView import/export and restore not tested. Supabase authenticated login UI and signed APK not implemented.
- Production NO-GO. Existing live release.json 75313 unchanged; no phone reset.
