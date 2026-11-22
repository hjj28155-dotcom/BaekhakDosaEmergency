# LOTTO645 75321 배포 점검 (2026-11-22)

- 기존 75320 APK와 비교: 기존 ZIP 엔트리 127개 유지, AndroidManifest.xml·assets/index.html·assets/LOTTO645_R161J_UNIQUE_auto.js 3개 변경
- 75321 APK ZIP 무결성 PASS
- Node.js 인공 과거자료 기반 코드검사 PASS: 4방 동일 공동후보 12수, 각 방 5조합, 전체 20조합 중복 없음, 동일 시드 재현, 다른 테스트 시드 조합 차이
- 실기기 덮어쓰기 설치, 기존 구매·DB·성적·PNG 보존, 공식 1252회 조합 성적: 미검증
- **전체 사용자 자동 배포 NO-GO**. 기존 main release.json 75313 유지
- 단계적 검증 필요: 구매원장 백업, 설치 전후 원장 대조, 새로운 회차 4방 분석, 기기별 조합 비교, 재시작 후 재현 확인
