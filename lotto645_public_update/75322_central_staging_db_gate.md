# ⚓ LOTTO645 중앙 배정 DB 시험 게이트 (2026-11-22)

- 별도 Supabase staging 스키마와 인증 사용자 RPC 생성. 기존 테이블 및 앱 업데이트 설정 불변.
- 동일 회차 2개 시험 계정에 20조합씩 배정: 합계 40개, 중복 0개 (시험 모두 ROLLBACK)
- 동일 계정 재요청: 기존 20개 재반환 확인
- Edge Function 배포 시도: 프로젝트 함수 수 한도로 실패. 기존 운영 함수 변경 없음.
- REST RPC 클라이언트 어댑터 작성, 아직 APK 미탑재.
- 사용자 인증/기기별 등록, WebView 실제 연결, 기존 구매원장 보존 시험, 여러 실제 휴대전화 시험 미완료.
- 연구 자료: https://drive.google.com/file/d/1jyS118GavlS8gZOVAuXbu0aoWpZQjGYR/view
- **프로덕션 NO-GO**. main release.json 75313 유지.
