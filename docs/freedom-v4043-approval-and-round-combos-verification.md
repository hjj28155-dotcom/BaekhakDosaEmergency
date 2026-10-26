# V4.0.43 승인·회차 조합 회귀 검증
- Branch-only release candidate based on V4.0.42. Original V4.0.41 Drive APK and central control v1.0.13 APK are NOT modified.
- Already-approved app uses native SharedPreferences 'approved' to skip requests; a server-confirmed approval now transitions straight to guide/home without an unnecessary second confirmation.
- New devices still require a legitimate initial approval; pending requests are checked, not duplicated. No forced auto approval.
- For recent rounds 1243–1247, real persisted 'analysis.auto20' is used if present. If missing, verified stored 'buy.combos' five combinations are shown without retroactive generation. If both absent, explicitly show no record. For 1247, offer to run the engine for the current round; do not fabricate historic result-before data.
- Public DB list is limited to recent 5 rounds; source raw_bank and user stored records are not erased. All 1–1247 originals remain in local storage/data.
- Central control remains captain-only separate APK; admin approvals stay outside distribution APK.
- Syntax validation: main embedded JS PASS, onboarding JS PASS. Need Android CI compile/sign, Android screen acceptance, approval and combo QA before fixed public Drive update.
